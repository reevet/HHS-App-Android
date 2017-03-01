package info.holliston.high.app.datamodel.download;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import info.holliston.high.app.datamodel.Article;

/*
 * The storage and retrieval vessel for feed articles
 */
public abstract class ArticleDataSource {

    protected Context context;
    private ArticleSQLiteHelper dbHelper;
    protected String[] allColumns = {
            ArticleSQLiteHelper.COLUMN_ID,
            ArticleSQLiteHelper.COLUMN_TITLE,
            ArticleSQLiteHelper.COLUMN_URL,
            ArticleSQLiteHelper.COLUMN_DATE,
            ArticleSQLiteHelper.COLUMN_KEY,
            ArticleSQLiteHelper.COLUMN_DETAILS,
            ArticleSQLiteHelper.COLUMN_IMGSRC};
    protected ArticleDataSourceOptions options;
    public String name;
    // Database fields
    private SQLiteDatabase database;

    public void setup(Context context, ArticleDataSourceOptions options){
        this.options = options;
        this.context = context;
        String databaseName = options.getDatabaseName();
        dbHelper = new ArticleSQLiteHelper(context, databaseName);
    }

    private void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    private void close() {
        dbHelper.close();
    }

    public String getName() {
        return this.options.getDatabaseName();
    }

    /*
     * Creates an Article object with the given information
     * and places it in the SQL database
     */
    Article createArticle(String title, String key, URL url, Date date, String details, String imgsrc) {
        Article newArticle;
        // is the article already exists, get it
        newArticle = articleFromKey(key);

        //prep the info for a SQL query
        ContentValues values = new ContentValues();

        values.put(ArticleSQLiteHelper.COLUMN_TITLE, title);

        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeSdf = new SimpleDateFormat("kk:mm");
        String dateString = dateSdf.format(date);
        String timeString = timeSdf.format(date);
        int tsio = timeString.indexOf("24");
        if (tsio == 0) {
            timeString = timeString.replace("24", "00");
        }
        String fullDateString = dateString + " " + timeString;

        values.put(ArticleSQLiteHelper.COLUMN_URL, url.toString());
        values.put(ArticleSQLiteHelper.COLUMN_DATE, fullDateString);
        values.put(ArticleSQLiteHelper.COLUMN_DETAILS, details);
        values.put(ArticleSQLiteHelper.COLUMN_IMGSRC, imgsrc);
        values.put(ArticleSQLiteHelper.COLUMN_KEY, key);

        // if the article does not exist, create/insert it.
        // if it does exist, update it
        if (newArticle == null) {
            this.open();
            //insert the article
            long insertId = database.insert(dbHelper.getName(), null,
                    values);
            //get it back
            Cursor cursor = database.query(dbHelper.getName(),
                    allColumns, ArticleSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                    null, null, null);
            cursor.moveToFirst();

            //new article = the newly inserted article
            newArticle = cursorToArticle(cursor);
            cursor.close();
            this.close();
        } else {
            values.put(ArticleSQLiteHelper.COLUMN_KEY, newArticle.key);
            this.open();
            //update the article with the new info
            //int update =
            database.update(dbHelper.getName(),
                    values, ArticleSQLiteHelper.COLUMN_URL + "='" + url.toString() + "'", null);
            //Log.d("ArticleDataSource", update+" records updated");
            Cursor cursor = database.query(dbHelper.getName(),
                    allColumns, ArticleSQLiteHelper.COLUMN_URL + "='" + url.toString() + "'", null,
                    null, null, null);
            cursor.moveToFirst();
            //new article = recently updated article
            newArticle = cursorToArticle(cursor);
            cursor.close();
            this.close();
        }
        return newArticle;
    }

    /*
     * batch method for adding a set of articles
     */
    void createArticles(List<Article> articleList) {
        for (Article art : articleList) {
            this.createArticle(art.title, art.key, art.url, art.date, art.details, art.imgSrc);
        }
    }

    /*
     * Deletes a single article. No need for it now, bu you never know
     */
    /*public void deleteArticle(Article article) {
        long id = article.id;
        System.out.println("Comment deleted with id: " + id);
        database.delete(ArticleSQLiteHelper.TABLE_SCHEDULES,
                ArticleSQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }*/

    /*
     * Finds an article with a given key
     */
    Article articleFromKey(String key) {
        Cursor cursor;
        Article article;
        try {
            this.open();
            cursor = database.query(dbHelper.getName(),
                    allColumns, ArticleSQLiteHelper.COLUMN_KEY + "= '" + key + "'",
                    null, null, null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                article = cursorToArticle(cursor);
                cursor.moveToNext();
            } else {
                article = null;
            }
            // make sure to close the cursor
            cursor.close();
            this.close();
        } catch (Exception e) {
            Log.e("ArticleDataSource", e.toString());
            article = null;
        }
        return article;
    }

    /*Just in case
    public void nukeAllRecords() {
        this.open();
        database.execSQL("delete from " + dbHelper.getName());
        this.close();
    }*/

    /*
     * Gets the most first Article in the list.
     * News = most recent; Events = next upcoming
     */
    public Article getLatestArticle() {
        Article returnArticle = new Article();
        List<Article> articles = getAllArticles();
        if (articles.size()>0) {
            returnArticle = articles.get(0);
        } else {
            returnArticle = null;
        }
        return returnArticle;
    }

    /*
     * Obtains all relevant articles in this datasource
     */
    public List<Article> getAllArticles() {
        List<Article> articles = new ArrayList<>();

        String orderBy;
        String where;

        //get the date (including anything since midnight)
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String nowString;
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        //build "orderBy" and "where" "based on whether we want future or past articles
        if (options.sortOrder == ArticleDataSourceOptions.SortOrder.GET_FUTURE) {
            now = cal.getTime();
            nowString = sdf.format(now);
            orderBy = ArticleSQLiteHelper.COLUMN_DATE + " ASC";
            where = ArticleSQLiteHelper.COLUMN_DATE + " >= '" + nowString + "'";
        } else {
            cal.add(Calendar.DATE, 1);
            now = cal.getTime();
            nowString = sdf.format(now);
            orderBy = ArticleSQLiteHelper.COLUMN_DATE + " DESC";
            where = ArticleSQLiteHelper.COLUMN_DATE + " <= '" + nowString + "'";
        }

        this.open();
        //get the articles
        Cursor cursor = database.query(dbHelper.getName(),
                allColumns, where, null, null, null, orderBy, null);

        cursor.moveToFirst();
        //cycle through the query result, adding articles to the temp list
        String tempKey = "";
        while (!cursor.isAfterLast()) {
            Article article = cursorToArticle(cursor);
            //tempKey prevents returning duplicates (just in case)
            if (!tempKey.equals(article.key)) {
                articles.add(article);
                tempKey = article.key;
            }
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        this.close();
        //return the temp list as the set of articles
        return articles;
    }

    /*
     * Turns a cursor object into a returnable Article
     */
    private Article cursorToArticle(Cursor cursor) {
        Article article = new Article();

        article.id = cursor.getLong(0);
        article.title = cursor.getString(1);
        String urlString = cursor.getString(2);
        try {
            article.url = new URL(urlString);
        } catch (Exception e) {
            article.url = null;
        }
        String dateString = cursor.getString(3);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        try {
            article.date = sdf.parse(dateString);
        } catch (Exception e) {
            article.date = new Date();
        }
        article.key = cursor.getString(4);
        article.details = cursor.getString(5);
        article.imgSrc = cursor.getString(6);
        return article;
    }


    /*
         * Starts the download/parsing process for this database.
         * First, it chooses which type (XML or JSON) to download
         */
    public String downloadArticles(ArticleParser.SourceMode refreshSource) {
        String result;
        List<Article> articles;
        articles = downloadArticlesFromNetwork(refreshSource);
        if (articles.size() > 0) {
            this.createArticles(articles);
            result = name+": "+ articles.size()+" downloaded";
        } else {
            result = name+": "+ "no articles found";
        }
        return result;
    }

    /*
     * each subclass Json or Xml has its own method for downloading and parsing
     */
    abstract List<Article> downloadArticlesFromNetwork(ArticleParser.SourceMode refreshSource);


    /*
     * Create the HTTP connection to the feed
     */
    protected InputStream downloadUrl(String urlString) throws IOException {
        java.net.URL url = new java.net.URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    /*
     * Cycle through the database and cache any images
     */
    public void downloadAndCacheImages() {
        List<Article> al = this.getAllArticles();
        if ((al != null) && (al.size() > 0)) {

            ImageAsyncCacher ial = new ImageAsyncCacher(
                    200, context, al);
            ial.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /*
     * A custom object for holding all of the datasource options
     */
    public static class ArticleDataSourceOptions {
        private String databaseName;
        private SourceType sourceType;
        private String urlString;
        private String[] parserNames;
        private ArticleParser.HtmlTags conversionType;
        private SortOrder sortOrder;
        private String limit;

        public ArticleDataSourceOptions(String databaseName, SourceType sourceType,
                                        String urlString, String[] parserNames,
                                        ArticleParser.HtmlTags conversionType,
                                        SortOrder sortOrder, String limit) {
            this.setDatabaseName(databaseName);
            this.setSourceType(sourceType);
            this.setUrlString(urlString);
            this.setParserNames(parserNames);
            this.setConversionType(conversionType);
            this.setSortOrder(sortOrder);
            this.setLimit(limit);
        }

        public String getDatabaseName() {
            return this.databaseName;
        }

        public void setDatabaseName(String name) {
            this.databaseName = name;
        }

        public String getUrlString() {
            return this.urlString;
        }
        public void setUrlString(String url) {
            this.urlString = url;
        }

        public String[] getParserNames() {
            return this.parserNames;
        }
        public void setParserNames(String[] names) {
            this.parserNames = names;
        }

        public void setSourceType(SourceType type) {
            this.sourceType = type;
        }

        public void setConversionType(ArticleParser.HtmlTags type) {
            this.conversionType = type;
        }
        public ArticleParser.HtmlTags getConversionType() {return this.conversionType;}

        public void setSortOrder(SortOrder order) {
            this.sortOrder = order;
        }

        public String getLimit() {
            return this.limit;
        }
        public void setLimit(String limit) {
            this.limit = limit;
        }

        public enum SortOrder {GET_FUTURE, GET_PAST}

        public enum SourceType {XML, JSON}


    }
}


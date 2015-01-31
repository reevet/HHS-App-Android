package info.holliston.high.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
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
import java.util.UUID;

import info.holliston.high.app.model.Article;
import info.holliston.high.app.xmlparser.ArticleParser;
import info.holliston.high.app.xmlparser.EventJsonParser;

public class ArticleDataSource{

    // Database fields
    private SQLiteDatabase database;
    private ArticleSQLiteHelper dbHelper;
    private String[] allColumns = {
            ArticleSQLiteHelper.COLUMN_ID,
            ArticleSQLiteHelper.COLUMN_TITLE,
            ArticleSQLiteHelper.COLUMN_URL,
            ArticleSQLiteHelper.COLUMN_DATE,
            ArticleSQLiteHelper.COLUMN_KEY,
            ArticleSQLiteHelper.COLUMN_DETAILS,
            ArticleSQLiteHelper.COLUMN_IMGSRC};
    private String[] parserNames;
    private String urlString;
    private ArticleParser.HtmlTags conversionType;
    public enum SortOrder {GET_FUTURE, GET_PAST}
    private SortOrder sortOrder;
    private String limit;

    public ArticleDataSource(Context context, ArticleDataSourceOptions options) {

        this.parserNames = options.getParserNames();
        this.urlString = options.getUrlString();
        this.conversionType = options.getConversionType();
        this.sortOrder = options.getSortOrder();
        this.limit = options.getLimit();
        String databaseName = options.getDatabaseName();
        dbHelper = new ArticleSQLiteHelper(context, databaseName);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Article createArticle(String title, URL url, Date date, String details, String imgsrc) {
        Article newArticle;
        newArticle = articleFromUrl(url);
            ContentValues values = new ContentValues();
            values.put(ArticleSQLiteHelper.COLUMN_TITLE, title);
            SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeSdf = new SimpleDateFormat("kk:mm");
            //SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a");
        String dateString = dateSdf.format(date);
            String timeString = timeSdf.format(date);
            int tsio = timeString.indexOf("24");
            if (tsio == 0) {
                timeString = timeString.replace("24", "00");
            }
            /*if (timeString.charAt(6) == 'P'){
                int hour = Integer.parseInt(timeString.substring(0,2));
                hour = hour+12;
                timeString = String.valueOf(hour)+timeString.substring(2,5);
            } else {
                timeString = timeString.substring(0,5);
            }*/
            String fullDateString = dateString+" "+timeString;
        values.put(ArticleSQLiteHelper.COLUMN_URL, url.toString());
            values.put(ArticleSQLiteHelper.COLUMN_DATE, fullDateString);
            values.put(ArticleSQLiteHelper.COLUMN_DETAILS, details);
            values.put(ArticleSQLiteHelper.COLUMN_IMGSRC, imgsrc);

        if (newArticle == null) {
            values.put(ArticleSQLiteHelper.COLUMN_KEY, UUID.randomUUID().toString());

            long insertId = database.insert(dbHelper.getName(), null,
                    values);
            Cursor cursor = database.query(dbHelper.getName(),
                    allColumns, ArticleSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                    null, null, null);
            cursor.moveToFirst();
            newArticle = cursorToArticle(cursor);
            cursor.close();
        } else {
            values.put(ArticleSQLiteHelper.COLUMN_KEY, newArticle.key.toString());

            int update = database.update(dbHelper.getName(),
                    values, ArticleSQLiteHelper.COLUMN_URL+"='"+url.toString()+"'", null
                    );
            Log.d("ArticleDataSource", update+" records updated");
            Cursor cursor = database.query(dbHelper.getName(),
                    allColumns, ArticleSQLiteHelper.COLUMN_URL+"='"+url.toString()+"'", null,
                    null, null, null);
            cursor.moveToFirst();
            newArticle = cursorToArticle(cursor);
            cursor.close();
        }
        return newArticle;
    }

    public void createArticles(List<Article> articleList) {
        for (Article art : articleList){
            this.createArticle(art.title, art.url, art.date, art.details, art.imgSrc);
        }
    }

    /*public void deleteArticle(Article article) {
        long id = article.id;
        System.out.println("Comment deleted with id: " + id);
        database.delete(ArticleSQLiteHelper.TABLE_SCHEDULES,
                ArticleSQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public Article articleFromKey(UUID key) {
        Cursor cursor = database.query(ArticleSQLiteHelper.TABLE_SCHEDULES,
                allColumns, ArticleSQLiteHelper.COLUMN_KEY+"="+key.toString(),
                null, null, null, null);

        Article article;
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            article = cursorToArticle(cursor);
            cursor.moveToNext();
        } else {
            article = null;
        }
        // make sure to close the cursor
        cursor.close();
        return article;
    }*/

    public Article articleFromUrl(URL url) {
        Cursor cursor;
        Article article;
        try {
            cursor = database.query(dbHelper.getName(),
                    allColumns, ArticleSQLiteHelper.COLUMN_URL+"='"+url.toString()+"'",
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
        } catch (Exception e) {
            Log.e("ArticleDataSource",e.toString());
            article = null;
        }

        return article;
    }

    public void nukeAllRecords() {
        database.execSQL("delete from "+ dbHelper.getName());
    }

    public List<Article> getAllArticles() {
        List<Article> articles = new ArrayList<Article>();

        String orderBy;
        String where;

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// kk:mm");
        String nowString;
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (this.sortOrder == SortOrder.GET_FUTURE) {
            now = cal.getTime();
            nowString = sdf.format(now);
            orderBy =  ArticleSQLiteHelper.COLUMN_DATE+" ASC";
            where = ArticleSQLiteHelper.COLUMN_DATE+" >= '"+nowString+"'";
        } else {
            cal.add(Calendar.DATE, 1);
            now = cal.getTime();
            nowString = sdf.format(now);
            orderBy = ArticleSQLiteHelper.COLUMN_DATE+" DESC";
            where = ArticleSQLiteHelper.COLUMN_DATE+" <= '"+nowString+"'";
        }

        Cursor cursor = database.query(dbHelper.getName(),
                allColumns, where, null, null, null, orderBy, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Article article = cursorToArticle(cursor);
            articles.add(article);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return articles;
    }

    public Article getArticle(int i) {
        String orderBy;
        String where;

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // kk:mm");
        String nowString;
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (this.sortOrder == SortOrder.GET_FUTURE) {
            now = cal.getTime();
            nowString = sdf.format(now);
            orderBy =  ArticleSQLiteHelper.COLUMN_DATE+" ASC";
            where = ArticleSQLiteHelper.COLUMN_DATE+" >= '"+nowString+"'";
        } else {
            cal.add(Calendar.DATE, 1);
            now = cal.getTime();
            nowString = sdf.format(now);
            orderBy = ArticleSQLiteHelper.COLUMN_DATE+" DESC";
            where = ArticleSQLiteHelper.COLUMN_DATE+" <= '"+nowString+"'";
        }

        Cursor cursor = database.query(dbHelper.getName(),
                allColumns, where, null, null, null, orderBy, this.limit);

        cursor.moveToFirst();
        Article article = null;
        int j=0;
        while (!cursor.isAfterLast()) {
            if (j==i) {
                article = cursorToArticle(cursor);
                break;
            }
            cursor.moveToNext();
            j++;
        }
        // make sure to close the cursor
        cursor.close();
        return article;
    }


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
        article.key = UUID.fromString(cursor.getString(4));
        article.details = cursor.getString(5);
        article.imgSrc = cursor.getString(6);
        return article;
    }

    public String downloadArticlesFromNetwork(ArticleParser.SourceMode refreshSource) {
        String result;

        int articlesCount = this.getAllArticles().size();

        if ((refreshSource == ArticleParser.SourceMode.DOWNLOAD_ONLY)
                || (refreshSource == ArticleParser.SourceMode.PREFER_DOWNLOAD)
                || (articlesCount <=0)){
            InputStream stream = null;
            try {
                // Instantiate the parser
                ArticleParser xmlParser = new ArticleParser(this, parserNames, this.conversionType, Integer.parseInt(this.limit));
                stream = downloadUrl(this.urlString);
                //List<Article> backup = this.getAllArticles();
                String parseResult = xmlParser.parse(stream);
                result = "Downloaded: "+parseResult;

                if (xmlParser.getAllArticles().size() > 0) {
                    this.nukeAllRecords();
                    this.createArticles(xmlParser.getAllArticles());
                }
                //testStore.privateItems = articles;
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (Exception e) {
                result = "Downloading error: "+e.toString()+". Using cache instead.";
            }
            finally  {
                try {
                    if (stream !=null){
                        stream.close();
                    }
                } catch (Exception e) {
                    //ignore
                }
            }
        } else {
            result = "Download skipped: "+articlesCount+" articles in cache (good enough)";
        }
        return result;
    }

    public String downloadEventsFromNetwork(ArticleParser.SourceMode refreshSource) {
        String result;

        int articlesCount = this.getAllArticles().size();

        if ((refreshSource == ArticleParser.SourceMode.DOWNLOAD_ONLY)
                || (refreshSource == ArticleParser.SourceMode.PREFER_DOWNLOAD)
                || (articlesCount <=0)){
            InputStream stream = null;
            try {
                // Instantiate the parser
                EventJsonParser jsonParser = new EventJsonParser(this, parserNames, this.conversionType, Integer.parseInt(this.limit));

                Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'00'%3A'00'%3A'00-05'%3A'00");
                String nowString = sdf.format(now);

                this.urlString = this.urlString+"&timeMin="+nowString;
                stream = downloadUrl(this.urlString);
                //List<Article> backup = this.getAllArticles();
                String parseResult = jsonParser.parse(stream);
                result = "Downloaded: "+parseResult;

                if (jsonParser.getAllArticles().size() > 0) {
                    this.nukeAllRecords();
                    this.createArticles(jsonParser.getAllArticles());
                }
                //testStore.privateItems = articles;
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (Exception e) {
                result = "Downloading error: "+e.toString()+". Using cache instead.";
            }
            finally  {
                try {
                    if (stream !=null){
                        stream.close();
                    }
                } catch (Exception e) {
                    //ignore
                }
            }
        } else {
            result = "Download skipped: "+articlesCount+" events in cache (good enough)";
        }
        return result;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
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
}


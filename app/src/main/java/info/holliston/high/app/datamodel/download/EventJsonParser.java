package info.holliston.high.app.datamodel.download;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import info.holliston.high.app.datamodel.Article;

class EventJsonParser {
    private String[] parserNames;

    private String entryName;
    private String titleName;
    private String linkName;
    private String dateName;
    private String detailsName;
    private String idName;

    private List<Article> articleList;

    public EventJsonParser(String[] parserNames) {
        this.parserNames = parserNames;
        this.articleList = new ArrayList<>();
    }

    public List<Article> getAllArticles() {
        return this.articleList;
    }

    public String parse(InputStream in) {
        String result = null;

        this.entryName = parserNames[0];
        this.titleName = parserNames[1];
        this.linkName = parserNames[2];
        this.dateName = parserNames[3];
        //String startTimeName = parserNames[4];
        this.detailsName = parserNames[5];
        this.idName = parserNames[6];

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            result = readFeed(sb.toString());
        } catch (Exception e) {
            result = "ArticleParser error: " + e.toString();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String readFeed(String input) throws XmlPullParserException, IOException {
        String result;
        int counter = 0;

        try {
            JSONObject calObj = new JSONObject(input);

            JSONArray items = calObj.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject event = items.getJSONObject(i);
                getAndStoreArticle(event);
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        result = "Articles downloaded: " + counter;
        return result;
        //return entries;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private void getAndStoreArticle(JSONObject event) {

        String title;
        String details;
        String link;
        String date;
        String id;

        title = readTitle(event);
        details = readSummary(event);
        link = readLink(event);
        date = readDate(event);
        id = readId(event);

        URL url = null;
        try {
            url = new URL(link);
        } catch (Exception e) {
            try {
                url = new URL("http://www.google.com");
                Log.d("parser", "Error making URL");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Date dateDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSSZ");

        if (date != null) {
            if (date.length() == 24) {
                format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS'Z'");
            } else if (date.length() == 25) {
                format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss-SS:00");
            } else if (date.length() == 10) {
                format = new SimpleDateFormat("yyyy-MM-dd");
            }
        }
        try {
            dateDate = format.parse(date);
        } catch (ParseException e) {
            dateDate = new Date();
            Log.d("parser", "Error making date");
        }

        if (id != null) {
            id = id.replace("/", "");
            id = id.replace("&", "");
            id = id.replace("?", "");
        }

        //the "null" below represents the Key, which we don't bother with for events
        Article tempArticle = new Article(title, id, url, dateDate, details, null);
        this.articleList.add(tempArticle);
    }

    // Processes title tags in the feed.
    private String readTitle(JSONObject event) {
        String title = "";
        try {
            title = event.getString(this.titleName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }

    // Processes link tags in the feed.
    private String readLink(JSONObject event) {
        String link = null;
        try {
            link = event.getString(this.linkName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return link;
    }

    private String readDate(JSONObject event) {
        String date = "";
        try {
            JSONObject startDateObj = event.getJSONObject(this.dateName);
            Iterator<String> keys = startDateObj.keys();
            while (keys.hasNext()) {
                // loop to get the dynamic key
                String currentDynamicKey = keys.next();

                // get the value of the dynamic key
                if (currentDynamicKey.contains("date")) {
                    date = startDateObj.getString(currentDynamicKey);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    private String readId(JSONObject event) {
        String id = null;
        try {
            id = event.getString(this.idName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    // Processes summary tags in the feed.
    private String readSummary(JSONObject event) {
        String summary = "";
        try {
            summary = event.getString(this.detailsName);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return summary;

    }
}

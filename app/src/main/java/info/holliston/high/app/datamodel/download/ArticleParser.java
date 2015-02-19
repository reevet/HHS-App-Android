package info.holliston.high.app.datamodel.download;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.holliston.high.app.datamodel.Article;

public class ArticleParser {
    private static final String ns = null;

    private String[] parserNames;

    // these are the values that are in the XML to indicate items
    // e.g. entryName = "entry", dateName = "startDate"
    private String entryName;
    private String titleName;
    private String linkName;
    private String dateName;
    private String startTimeName;
    private String detailsName;
    private String idName;

    //holder for the image source
    private String currentImgSrc;
    private HtmlTags htmlTags;
    //counters
    private int limit;
    private int counter = 0;
    private List<Article> articleList;

    public ArticleParser(String[] parserNames, HtmlTags conversionType, int limit) {
        this.parserNames = parserNames;
        this.htmlTags = conversionType;
        this.limit = limit;
        this.articleList = new ArrayList<>();
    }

    /*
     * Get the articles from this parser
     */
    public List<Article> getAllArticles() {
        return this.articleList;
    }

    /*
     * Main method that pulls the data to parse
     */
    public String parse(InputStream in) throws XmlPullParserException, IOException {
        String result = "";

        this.entryName = parserNames[0];
        this.titleName = parserNames[1];
        this.linkName = parserNames[2];
        this.dateName = parserNames[3];
        this.startTimeName = parserNames[4];
        this.detailsName = parserNames[5];
        this.idName = parserNames[6];

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            // send parser data to "readFeed" method to extract the info
            result = readFeed(parser);
        } catch (Exception e) {
            result = "ArticleParser error: " + e.toString();
        } finally {
            in.close();
        }
        //returns a result string to the sender. If satisfied, the sender can then
        //send a getAllArticles() request for the actual dat
        return result;
    }

    /*
     * Takes a parse object and extracts the info
     * Serially reads tags to find articles, and then
     * sends them out for processing
     */
    private String readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result;
        //require that "feed" is the first containing tag
        parser.require(XmlPullParser.START_TAG, ns, "feed");

        // within "feed", continue to find start tags until end of "feed" is reached
        // or until we have read enough articles (by reaching the "limit"), e.g. read 30 schedules.
        while ((parser.next() != XmlPullParser.END_TAG) && (this.counter < this.limit)) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                //ignore end tags while looking for an article
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(entryName)) {
                // if this is the start tag for an article, send the parser to
                // readEntry to process the article
                readEntry(parser);
                counter++;
            } else {
                skip(parser);
            }
        }
        result = "Articles downloaded: " + counter;
        return result;
        //return entries;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private void readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "entry");
        String title = null;
        String details = null;
        String link = null;
        String date = null;
        String id = null;
        this.currentImgSrc = null;

        // an end tag here means the end of the article
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(titleName)) {
                title = readTitle(parser);
            } else if (name.equals(detailsName)) {
                details = readSummary(parser);
            } else if (name.equals(linkName)) {
                link = readLink(parser, link);
            } else if (name.equals(dateName)) {
                date = readDate(parser);
            } else if (name.equals(idName)) {
                id = readId(parser);
            } else {
                skip(parser);
            }
        }

        //now that the article is processed, prep it to be stored in the datasource
        URL url;
        try {
            url = new URL(link);
        } catch (Exception e) {
            url = new URL("http://www.google.com");
            Log.d("parser", "Error making URL");
        }

        //parse possible date formats
        Date dateDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSSZ");

        if (date != null) {
            if (date.length() == 24) {
                format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS'Z'");
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
        //These replacements SHOULD be redundant, but just in case....
        if (id != null) {
            id = id.replace("/", "");
            id = id.replace("&", "");
            id = id.replace("?", "");
        }

        //make an Article with the data, and store in the temporary (parser's) articleList
        Article tempArticle = new Article(title, id, url, dateDate, details, this.currentImgSrc);
        this.articleList.add(tempArticle);
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, titleName);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, titleName);
        return title;
    }

    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser, String oldLink) throws IOException, XmlPullParserException {
        String link = null;
        parser.require(XmlPullParser.START_TAG, ns, linkName);
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");
        if (tag.equals(linkName)) {
            if (relType.equals("alternate")) {
                link = parser.getAttributeValue(null, "href");
            }
        }
        parser.nextTag();
        if (link == null) {
            link = oldLink;
        }
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    private String readDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        String date;
        parser.require(XmlPullParser.START_TAG, ns, dateName);
        if (!startTimeName.equals("")) {
            //if (dateType.equals("startTime")){
            date = parser.getAttributeValue(null, startTimeName);
            parser.nextTag();
            //}
        } else {
            date = readText(parser);
        }
        parser.require(XmlPullParser.END_TAG, ns, dateName);
        return date;
    }

    // Processes summary tags in the feed.
    private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
        String summary;
        parser.require(XmlPullParser.START_TAG, ns, detailsName);
        if (this.htmlTags == HtmlTags.IGNORE_HTML_TAGS) {
            summary = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, detailsName);
        } else {
            StringBuilder sb = new StringBuilder();
            Boolean done = false;
            while (!done) {
                int next = parser.next();
                if (next == XmlPullParser.TEXT) {
                    sb.append(parser.getText());
                } else if (next == XmlPullParser.START_TAG) {
                    if (this.htmlTags == HtmlTags.KEEP_HTML_TAGS) {
                        sb.append("<").append(parser.getName());
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            sb.append(" ").append(parser.getAttributeName(i)).append("='");
                            sb.append(parser.getAttributeValue(i)).append("\'");
                        }
                        sb.append(">");
                    }
                    if (parser.getName().equals("img") && (this.currentImgSrc == null)) {
                        this.currentImgSrc = parser.getAttributeValue(null, "src");
                    }

                } else if (next == XmlPullParser.END_TAG) {
                    if (htmlTags == HtmlTags.KEEP_HTML_TAGS) {
                        if (!(parser.getName().equals(detailsName))) {
                            sb.append("</").append(parser.getName()).append(">");
                        } else {
                            done = true;
                        }
                    } else if (htmlTags == HtmlTags.CONVERT_LINE_BREAKS) {
                        if ((parser.getName().equals("p")) || (parser.getName().equals("br")) || (parser.getName().equals("div"))
                                || (parser.getName().equals("ul")) || (parser.getName().equals("table"))) {
                            sb.append("\n");
                        } else if (parser.getName().equals(detailsName)) {
                            done = true;
                        }
                    }
                }
            }
            summary = sb.toString();
        }
        return summary;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Processes title tags in the feed.
    private String readId(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, idName);
        String id = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, idName);
        return id;
    }

    // skips any other tags, but manages the end tag so the article doesn't end yet.
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    // marker for how to process HTML
    public enum HtmlTags {
        KEEP_HTML_TAGS, CONVERT_LINE_BREAKS, IGNORE_HTML_TAGS
    }

    // marker for where to get the data from
    public enum SourceMode {
        ALLOW_BOTH, DOWNLOAD_ONLY, CACHE_ONLY, PREFER_DOWNLOAD
    }
}

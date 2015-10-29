package info.holliston.high.app.datamodel.download;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import info.holliston.high.app.datamodel.Article;

/**
 * Created by Tom on 10/16/2015.
 */
public class XmlArticleDataSource extends ArticleDataSource{

    public XmlArticleDataSource(Context context, ArticleDataSource.ArticleDataSourceOptions options) {
        this.setup(context, options);
    }

    @Override
    List<Article> downloadArticlesFromNetwork(ArticleParser.SourceMode refreshSource) {
        String result;
        List<Article> returnArticles = null;

        int articlesCount = this.getAllArticles().size();
        // if datasource is empty or downloading is OK...
        if ((refreshSource == ArticleParser.SourceMode.DOWNLOAD_ONLY)
                || (refreshSource == ArticleParser.SourceMode.PREFER_DOWNLOAD)
                || (articlesCount <= 0)) {
            InputStream stream = null;
            try {
                // Instantiate the parser
                ArticleParser xmlParser = new ArticleParser(options.getParserNames(), options.getConversionType(), Integer.parseInt(options.getLimit()));
                String urlString = options.getUrlString();

                stream = downloadUrl(urlString);
                // download and parse
                String parseResult = xmlParser.parse(stream);
                result = "Downloaded: " + parseResult;

                returnArticles = xmlParser.getAllArticles();

            } catch (Exception e) {
                result = "Downloading error: " + e.toString() + ". Using cache instead.";
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (Exception e) {
                    //ignore
                }
            }
        } else {
            result = "Download skipped: " + articlesCount + " articles in cache (good enough)";
        }
        return returnArticles;
    }
}
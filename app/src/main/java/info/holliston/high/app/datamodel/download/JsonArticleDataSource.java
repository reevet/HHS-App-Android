package info.holliston.high.app.datamodel.download;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.List;

import info.holliston.high.app.datamodel.Article;

/**
 * Created by Tom on 10/16/2015.
 */
public class JsonArticleDataSource extends ArticleDataSource {

    public JsonArticleDataSource(Context context, ArticleDataSourceOptions options) {
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
                EventJsonParser jsonParser = new EventJsonParser(options.getParserNames());

                String urlString = options.getUrlString();
                urlString = urlString + "&fetchImages=true";
                stream = downloadUrl(urlString);

                //download and parse
                String parseResult = jsonParser.parse(stream);
                result = "Downloaded: " + parseResult;
                returnArticles = jsonParser.getAllArticles();


            } catch (Exception e) {
                //result = "Downloading error: " + e.toString() + ". Using cache instead.";
                Log.e("JsonADS", e.toString());
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
            result = "Download skipped: " + articlesCount + " events in cache (good enough)";
        }
        return returnArticles;
    }
}

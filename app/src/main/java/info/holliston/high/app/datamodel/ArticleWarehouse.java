package info.holliston.high.app.datamodel;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.download.ArticleDataSource;
import info.holliston.high.app.datamodel.download.ArticleDataSourceOptions;
import info.holliston.high.app.datamodel.download.ArticleParser;
import info.holliston.high.app.datamodel.download.ArticleSQLiteHelper;
import info.holliston.high.app.datamodel.download.CalArticleDataSource;
import info.holliston.high.app.datamodel.download.JsonArticleDataSource;
import info.holliston.high.app.datamodel.download.XmlArticleDataSource;

/**
 * Holds all ArticleDataStores for the active app
 */

public class ArticleWarehouse {

    private Map<StoreType, ArticleDataSource> sources;
    public ArticleWarehouse(Context context) {
        JsonArticleDataSource sNewsSource;
        XmlArticleDataSource sDailyannSource;
        CalArticleDataSource sScheduleSource;
        CalArticleDataSource sEventsSource;
        CalArticleDataSource sLunchSource;
        ArticleDataSourceOptions options;

        sources = new HashMap<>();

        options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_NEWS,
                ArticleDataSourceOptions.SourceType.JSON,
                context.getString(R.string.news_url),
                context.getResources().getStringArray(R.array.news_parser_names),
                ArticleParser.HtmlTags.KEEP_HTML_TAGS,
                ArticleDataSourceOptions.SortOrder.GET_PAST,
                "1");
        sNewsSource = new JsonArticleDataSource(context, options);
        sources.put(StoreType.NEWS, sNewsSource);

        options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_SCHEDULES,
                ArticleDataSourceOptions.SourceType.JSON,
                context.getString(R.string.schedules_url),
                context.getResources().getStringArray(R.array.schedules_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS,
                ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "2");
        sScheduleSource = new CalArticleDataSource(context, options);
        sources.put(StoreType.SCHEDULES, sScheduleSource);

        options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_LUNCH,
                ArticleDataSourceOptions.SourceType.JSON,
                context.getString(R.string.lunch_url),
                context.getResources().getStringArray(R.array.lunch_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS,
                ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "5");
        sLunchSource = new CalArticleDataSource(context, options);
        sources.put(StoreType.LUNCH, sLunchSource);

        options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_DAILYANN,
                ArticleDataSourceOptions.SourceType.XML,
                context.getString(R.string.dailyann_url),
                context.getResources().getStringArray(R.array.dailyann_parser_names),
                ArticleParser.HtmlTags.CONVERT_LINE_BREAKS,
                ArticleDataSourceOptions.SortOrder.GET_PAST,
                "1");
        sDailyannSource = new XmlArticleDataSource(context, options);
        sources.put(StoreType.DAILYANN, sDailyannSource);

        options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_EVENTS,
                ArticleDataSourceOptions.SourceType.JSON,
                context.getString(R.string.events_url),
                context.getResources().getStringArray(R.array.events_parser_names),
                ArticleParser.HtmlTags.CONVERT_LINE_BREAKS,
                ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "20");
        sEventsSource = new CalArticleDataSource(context, options);
        sources.put(StoreType.EVENTS, sEventsSource);
    }

    public List<Article> getAllArticles(StoreType type) {
        ArticleDataSource source = sources.get(type);
        return source.getAllArticles();
    }

    public String refreshData(StoreType type) {
        ArticleDataSource source = sources.get(type);
        return source.downloadArticles(ArticleParser.SourceMode.PREFER_DOWNLOAD);
    }

    public enum StoreType {NEWS, EVENTS, SCHEDULES, DAILYANN, LUNCH}
}


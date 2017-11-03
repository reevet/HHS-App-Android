package info.holliston.high.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.download.ArticleDataSource;
import info.holliston.high.app.datamodel.download.ArticleDataSourceOptions;
import info.holliston.high.app.datamodel.download.ArticleParser;
import info.holliston.high.app.datamodel.download.ArticleSQLiteHelper;
import info.holliston.high.app.datamodel.download.JsonArticleDataSource;

/**
 * Implementation of App Widget functionality.
 */
public class HHSWidget extends AppWidgetProvider {
    public static final String NOTIFICATION = "info.holliston.high.widget";

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        ArticleDataSource datasource;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        ArticleDataSourceOptions options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_SCHEDULES,
                ArticleDataSourceOptions.SourceType.JSON,
                context.getString(R.string.schedules_url),
                context.getResources().getStringArray(R.array.schedules_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS,
                ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "2");
        datasource = new JsonArticleDataSource(context, options);

        List<Article> articles;
        articles = datasource.getAllArticles();

        if (articles.size() > 0) {
            Article article = articles.get(0);

            if (articles.size() >= 2) {
                Date todayDate = new Date();
                Calendar todayCal = Calendar.getInstance();
                todayCal.setTime(todayDate);
                int todayMonth = todayCal.get(Calendar.MONTH);
                int todayDay = todayCal.get(Calendar.DATE);
                int todayHour = todayCal.get(Calendar.HOUR_OF_DAY);

                if (todayHour >= 14) {
                    Date firstDate = articles.get(0).date;
                    Calendar firstCal = Calendar.getInstance();
                    firstCal.setTime(firstDate);
                    int firstMonth = firstCal.get(Calendar.MONTH);
                    int firstDay = firstCal.get(Calendar.DATE);

                    if ((todayMonth == firstMonth) && (todayDay == firstDay)) {
                        article = articles.get(1);
                    }
                }
            }

            SimpleDateFormat df = new SimpleDateFormat("EEEE, MMM d");
            String dateString = df.format(article.date);

            views.setTextViewText(R.id.widget_label, "Holliston HS Schedule");
            views.setTextViewText(R.id.widget_date, dateString);
            views.setTextViewText(R.id.widget_title, article.title);

            char initial = article.title.charAt(0);

            switch (initial) {
                case 'A':
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.a_lg);
                    break;
                case 'B':
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.b_lg);
                    break;
                case 'C':
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.c_lg);
                    break;
                case 'D':
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.d_lg);
                    break;
                default:
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.star_lg);
                    break;
            }

            //onClickListener
            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction(NOTIFICATION);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);

            views.setOnClickPendingIntent(R.id.widget_all, pendingIntent);


            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        //ComponentName thisWidget = new ComponentName(context, HHSWidget.class);
        //int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        //final int N = appWidgetIds.length;
        //for (int i=0; i<N; i++) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        //appWidgetManager.updateAppWidget(appWidgetIds, view);

        //Intent intent2 = new Intent(context, ArticleDownloaderService.class);
        //intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allWidgetIds );
        //context.startService(intent2);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}



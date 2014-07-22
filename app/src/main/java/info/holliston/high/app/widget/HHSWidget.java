package info.holliston.high.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import info.holliston.high.app.ArticleDataSource;
import info.holliston.high.app.ArticleDataSourceOptions;
import info.holliston.high.app.ArticleDownloaderService;
import info.holliston.high.app.ArticleSQLiteHelper;
import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.xmlparser.ArticleParser;

/**
 * Implementation of App Widget functionality.
 */
public class HHSWidget extends AppWidgetProvider {
    public static final String NOTIFICATION = "info.holliston.high.widget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        ComponentName thisWidget = new ComponentName(context, HHSWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }

        //onClickListener
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent, 0);

        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        view.setOnClickPendingIntent(R.id.widget_all, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, view);

        //Intent intent2 = new Intent(context, ArticleDownloaderService.class);
        //intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allWidgetIds );
        //context.startService(intent2);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);
/*
        Bundle extras = intent.getExtras();
        if (extras != null) {
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context
                    .getPackageName(), HHSWidget.class.getName());
            int[] appWidgetIds = appWidgetManager
                    .getAppWidgetIds(thisAppWidget);

            onUpdate(context, appWidgetManager, appWidgetIds);
        }
        */
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        ArticleDataSource datasource;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        ArticleDataSourceOptions options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_SCHEDULES, context.getString(R.string.schedules_url),
                context.getResources().getStringArray(R.array.schedules_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS, ArticleDataSource.SortOrder.GET_FUTURE,
                "2");
        datasource = new ArticleDataSource(context,options);
        datasource.open();

        List<Article> articles;
        articles = datasource.getAllArticles();
        datasource.close();

        if (articles.size() >0)
        {
            Article article = articles.get(0);

            Calendar now = Calendar.getInstance();
            int nowMonth = now.get(Calendar.MONTH);
            int nowDate = now.get(Calendar.DATE);
            int nowHour = now.get(Calendar.HOUR_OF_DAY);
            int nowMinute = now.get(Calendar.MINUTE);
            Boolean after1330 = (nowHour>13) || ((nowHour == 13) && (nowMinute >30));

            Calendar firstArtCal = Calendar.getInstance();
            firstArtCal.setTime(article.date);
            int firstArtMonth = firstArtCal.get(Calendar.MONTH);
            int firstArtDate = firstArtCal.get(Calendar.DATE);

            if ((nowDate == firstArtDate) && (nowMonth == firstArtMonth) && (after1330)) {
               if (articles.size() >=2) {
                   article = articles.get(1);
               }
            }

            SimpleDateFormat df = new SimpleDateFormat("EEEE, MMM d");
            String dateString = df.format(article.date);

            views.setTextViewText(R.id.widget_label, "Holliston HS Schedule");
            views.setTextViewText(R.id.widget_date, dateString);
            views.setTextViewText(R.id.widget_title, article.title);

            char initial = article.title.charAt(0);

            switch (initial) {
                case 'A' :
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.a_50);
                    break;
                case 'B' :
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.b_50);
                    break;
                case 'C' :
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.c_50);
                    break;
                case 'D' :
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.d_50);
                    break;
                default :
                    views.setImageViewResource(R.id.widget_sched_icon, R.drawable.star_50);
                    break;
            }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }
}



package info.holliston.high.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import info.holliston.high.app.widget.HHSWidget;
import info.holliston.high.app.xmlparser.ArticleParser;

public class ArticleDownloaderService extends Service {

    private ArticleParser.SourceMode refreshSource = ArticleParser.SourceMode.ALLOW_BOTH;
    public static final String NOTIFICATION = "info.holliston.high.service.receiver";
    Boolean newNewsAvailable = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //--
        ArticleParser.SourceMode sentMode = (ArticleParser.SourceMode) intent.getSerializableExtra("refreshSource");
        String alarmSource = intent.getStringExtra("alarm");
        Log.d("ArticleDownloaderService", "Service started due to " +alarmSource);
        if (!(sentMode == null)){
            this.refreshSource = sentMode;

        }
        new PrefetchData().execute();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class PrefetchData extends AsyncTask<Void, Void, Void> {

        ArticleDataSource schedulesDataSource;
        ArticleDataSource dailyAnnDataSource;
        ArticleDataSource newsDataSource;
        ArticleDataSource eventsDataSource;
        ArticleDataSource lunchDataSource;

        String schedulesString = "";
        String newsString = "";
        String eventsString = "";
        String dailyAnnString = "";
        String lunchString = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ArticleDataSourceOptions sdso = new ArticleDataSourceOptions(
                    ArticleSQLiteHelper.TABLE_SCHEDULES, getString(R.string.schedules_url),
                    getResources().getStringArray(R.array.schedules_parser_names),
                    ArticleParser.HtmlTags.IGNORE_HTML_TAGS, ArticleDataSource.SortOrder.GET_FUTURE,
                    "25");
            schedulesDataSource = new ArticleDataSource(getApplicationContext(), sdso);

            ArticleDataSourceOptions dadso = new ArticleDataSourceOptions(
                    ArticleSQLiteHelper.TABLE_DAILYANN, getString(R.string.dailyann_url),
                    getResources().getStringArray(R.array.dailyann_parser_names),
                    ArticleParser.HtmlTags.CONVERT_LINE_BREAKS, ArticleDataSource.SortOrder.GET_PAST,
                    "10");
            dailyAnnDataSource = new ArticleDataSource(getApplicationContext(), dadso );

            ArticleDataSourceOptions ndso = new ArticleDataSourceOptions(
                    ArticleSQLiteHelper.TABLE_NEWS, getString(R.string.news_url),
                    getResources().getStringArray(R.array.news_parser_names),
                    ArticleParser.HtmlTags.KEEP_HTML_TAGS, ArticleDataSource.SortOrder.GET_PAST,
                    "25");
            newsDataSource = new ArticleDataSource(getApplicationContext(),ndso);

            ArticleDataSourceOptions edso = new ArticleDataSourceOptions(
                    ArticleSQLiteHelper.TABLE_EVENTS, getString(R.string.events_url),
                    getResources().getStringArray(R.array.events_parser_names),
                    ArticleParser.HtmlTags.IGNORE_HTML_TAGS, ArticleDataSource.SortOrder.GET_FUTURE,
                    "40");
            eventsDataSource = new ArticleDataSource(getApplicationContext(), edso
                    );

            ArticleDataSourceOptions ldso = new ArticleDataSourceOptions(
                    ArticleSQLiteHelper.TABLE_LUNCH, getString(R.string.lunch_url),
                    getResources().getStringArray(R.array.lunch_parser_names),
                    ArticleParser.HtmlTags.IGNORE_HTML_TAGS, ArticleDataSource.SortOrder.GET_FUTURE,
                    "40");
            lunchDataSource = new ArticleDataSource(getApplicationContext(), ldso
            );

            // before making http calls

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //nukeDatabaseRecords();
            //nukeCache();
            try {
                schedulesDataSource.open();
                String result = schedulesDataSource.downloadEventsFromNetwork(refreshSource);
                schedulesDataSource.close();
                Log.d("ArticleDownloaderService", "Schedules: "+result);
            } catch (Exception e) {
                schedulesString = getResources().getString(R.string.xml_error);
            }

            try {
                eventsDataSource.open();
                String result = eventsDataSource.downloadEventsFromNetwork(refreshSource);
                eventsDataSource.close();
                Log.d("ArticleDownloaderService", "Events: "+result);
            } catch (Exception e) {
                eventsString = getResources().getString(R.string.xml_error);
            }

            try {
                newsDataSource.open();
                String result = newsDataSource.downloadArticlesFromNetwork(refreshSource);
                newsDataSource.close();
                Log.d("ArticleDownloaderService", "News: "+result);
            } catch (Exception e) {
                newsString = getResources().getString(R.string.xml_error);
            }

            try {
                dailyAnnDataSource.open();
                String result = dailyAnnDataSource.downloadArticlesFromNetwork(refreshSource);
                dailyAnnDataSource.close();
                Log.d("ArticleDownloaderService", "Daily Announcements: "+result);
            } catch (Exception e) {
                dailyAnnString = getResources().getString(R.string.xml_error);
            }

            try {
                lunchDataSource.open();
                String result = lunchDataSource.downloadEventsFromNetwork(refreshSource);
                lunchDataSource.close();
                Log.d("ArticleDownloaderService", "Lunch: "+result);
            } catch (Exception e) {
                lunchString = getResources().getString(R.string.xml_error);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            publishResults("Success!");
            scheduleDownloads();
            refreshSource = ArticleParser.SourceMode.ALLOW_BOTH;
        }

        /*private void nukeDatabaseRecords() {
            schedulesDataSource.nukeAllRecords();
            eventsDataSource.nukeAllRecords();
            newsDataSource.nukeAllRecords();
            dailyAnnDataSource.nukeAllRecords();

        }*/
    }

    private void publishResults(String result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("result", result);
        intent.putExtra("newNewsAvailable", newNewsAvailable);
        sendBroadcast(intent);

        Intent intent2 = new Intent(this,HHSWidget.class);
        intent2.setAction("android.appwidget.action.APPWIDGET_UPDATE");
// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
// since it seems the onUpdate() is only fired on that:
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), HHSWidget.class));
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent2);

        Log.d("ArticleDownloaderService", "Results published as intents");
    }

    private void scheduleDownloads() {
        //cancelAlarm(0);
        //cancelAlarm(1);
        //cancelAlarm(2);
        cancelAlarm(10); //for testing only

        Random r = new Random();
        int val = r.nextInt(15); //between 14 and 0;
        int sign = ~(r.nextInt(2));
        int jitter = sign*val;

        //Alarm 0 - 4 am
        setAlarm(0, 4, jitter, AlarmManager.INTERVAL_DAY);

        //Alarm 1 - 8:30 am
        setAlarm(1, 8, 30+jitter, AlarmManager.INTERVAL_DAY);

        //Alarm 2 - 1:30 pm
        setAlarm(2, 13, 30+jitter, AlarmManager.INTERVAL_DAY);

        //Alarm 10 - Testing only
        //TODO: This is every 30 seconds! Disable before publishing!
        //setTestAlarm();

    }

    private void setAlarm(int alarm, int hour, int minute, long interval) {
        Date now = new Date();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);
        int nowHour = nowCal.get(Calendar.HOUR_OF_DAY);
        int nowMinute = nowCal.get(Calendar.MINUTE);
        if ((nowHour >hour) ||
                ((nowHour == hour) && (nowMinute >= minute))) {
        nowCal.set(Calendar.DATE, nowCal.get(Calendar.DATE)+1);
        }
        nowCal.set(Calendar.HOUR_OF_DAY, hour);
        nowCal.set(Calendar.MINUTE, minute);

        AlarmManager alarmMgr;
        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
        intent.putExtra("refreshSource", ArticleParser.SourceMode.DOWNLOAD_ONLY);
        intent.putExtra("alarm", "alarm-"+alarm);

        PendingIntent alarmIntent = PendingIntent.getService(getApplicationContext(), alarm, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC, nowCal.getTimeInMillis(),
                interval, alarmIntent);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, hh:mm a");
        Date setFor = nowCal.getTime();
        String setString = sdf.format(setFor);
        Log.d("ArticleDownloaderService", "Alarm "+alarm+" set for "+setString);
    }

    private void cancelAlarm(int alarm) {
        AlarmManager alarmMgr;
        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);

        PendingIntent alarmIntent = PendingIntent.getService(getApplicationContext(), alarm, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.cancel(alarmIntent);
    }

    /*private void setTestAlarm() {
        Date now = new Date();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);
        nowCal.set(Calendar.SECOND, nowCal.get(Calendar.SECOND) + 30);

        AlarmManager alarmMgr;
        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
        intent.putExtra("refreshSource", ArticleParser.SourceMode.DOWNLOAD_ONLY);
        intent.putExtra("alarm", "alarm-"+10);

        PendingIntent alarmIntent = PendingIntent.getService(getApplicationContext(), 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC, nowCal.getTimeInMillis(),
                1000*30, alarmIntent);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, hh:mm a");
        Date setFor = nowCal.getTime();
        String setString = sdf.format(setFor);
        Log.d("ArticleDownloaderService", "TestAlarm(10) set for "+setString);
    }*/



}


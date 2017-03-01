package info.holliston.high.app.datamodel.download;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.widget.HHSWidget;

public class ArticleDownloaderService extends Service {

    public static final String APP_RECEIVER = "info.holliston.high.service.receiver";
    private Boolean cacheImages = false;
    private ArticleDataSource schedulesDataSource;
    private ArticleDataSource dailyAnnDataSource;
    private ArticleDataSource newsDataSource;
    private ArticleDataSource eventsDataSource;
    private ArticleDataSource lunchDataSource;
    private ArticleParser.SourceMode refreshSource = ArticleParser.SourceMode.ALLOW_BOTH;
    private String alarmSource;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "ArticleDownloaderService started", Toast.LENGTH_LONG).show();

        // alarmReset says to reset alarms without refreshing data
        String alarmReset;
        alarmReset = intent.getStringExtra("alarmReset");
        if (alarmReset != null) {
            scheduleDownloads();
            return Service.START_NOT_STICKY;
        }

        /*// getImages says whether to re-cache all images
        String gi;
        gi = intent.getStringExtra("getImages");
        if (gi!=null && gi.equals("DOWNLOAD_ONLY")) {
            this.cacheImages = true;
        }*/
        this.cacheImages = false;

        // alarmSource says which alarm triggered this service
        alarmSource = intent.getStringExtra("alarm");
        Log.d("ArticleDownloaderService", "Service started due to " + alarmSource);

        // refreshSource says how to refresh (download, cache, or allow both)
        ArticleParser.SourceMode sentMode = (ArticleParser.SourceMode) intent.getSerializableExtra("refreshSource");
        if (!(sentMode == null)) {
            this.refreshSource = sentMode;
        }

        String sourcesToRefresh;
        sourcesToRefresh = intent.getStringExtra("datasources");
        if (sourcesToRefresh == null) {sourcesToRefresh = "all";}

        if (sourcesToRefresh.equals("news")) {
            refreshNews();
        } else {
            refreshNews();
            refreshDailyAnn();
            refreshSchedules();
            refreshEvents();
            refreshLunch();
            scheduleDownloads();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "ArticleDownloaderService KILLED", Toast.LENGTH_LONG).show();
    }

    /*
         * Each refresh method defines the datasources and options, and
         * then calls an asyncTask to download the feed
         */
    private void refreshSchedules() {
        ArticleDataSource.ArticleDataSourceOptions sdso = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_SCHEDULES,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.JSON,
                getString(R.string.schedules_url),
                getResources().getStringArray(R.array.schedules_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "25");
        schedulesDataSource = new CalArticleDataSource(getApplicationContext(), sdso);

        new refreshDataSource(schedulesDataSource, false, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void refreshNews() {
        ArticleDataSource.ArticleDataSourceOptions ndso = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_NEWS,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.JSON,
                getString(R.string.news_url) + "?key=" + getString(R.string.api_key),
                getResources().getStringArray(R.array.news_parser_names),
                ArticleParser.HtmlTags.KEEP_HTML_TAGS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_PAST,
                "25");
        newsDataSource = new JsonArticleDataSource(getApplicationContext(), ndso);
        new refreshDataSource(newsDataSource, true, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void refreshDailyAnn() {
        ArticleDataSource.ArticleDataSourceOptions dadso = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_DAILYANN,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.XML,
                getString(R.string.dailyann_url),
                getResources().getStringArray(R.array.dailyann_parser_names),
                ArticleParser.HtmlTags.CONVERT_LINE_BREAKS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_PAST,
                "10");
        dailyAnnDataSource = new XmlArticleDataSource(getApplicationContext(), dadso);
        new refreshDataSource(dailyAnnDataSource, true, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void refreshEvents() {
        ArticleDataSource.ArticleDataSourceOptions edso = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_EVENTS,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.JSON,
                getString(R.string.events_url),
                getResources().getStringArray(R.array.events_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "40");
        eventsDataSource = new CalArticleDataSource(getApplicationContext(), edso);
        new refreshDataSource(eventsDataSource, true, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void refreshLunch() {
        ArticleDataSource.ArticleDataSourceOptions ldso = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_LUNCH,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.JSON,
                getString(R.string.lunch_url),
                getResources().getStringArray(R.array.lunch_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "40");
        lunchDataSource = new CalArticleDataSource(getApplicationContext(), ldso);
        new refreshDataSource(lunchDataSource, true, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * send intents to let the activity know that some downloads are complete
     */
    private void publishResults(String name, String result) {

        // tell MainActivity that datasource has finished downloading
        Intent intent = new Intent(APP_RECEIVER);
        intent.putExtra("result", result);
        intent.putExtra("datasource", name);
        sendBroadcast(intent);

        //tell the home page widget that the datasource has finished downloading
        Intent intent2 = new Intent(this, HHSWidget.class);
        intent2.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), HHSWidget.class));
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent2);

        //Log.d("ArticleDownloaderService", "Results published for "+name );
    }

    /*
     * set alarms so that data will refresh in the background
     * based on the user's preferences
     */
    private void scheduleDownloads() {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // syncPref codes:
        // 1 = daily
        // 3 = am, advisory, pm (this is default)
        // 24 = hourly
        // 0 = never

        String syncPrefString = prefs.getString("sync_frequency", "3");
        int syncPref;
        try {
            syncPref = Integer.parseInt(syncPrefString);
        } catch (NumberFormatException nfe) {
            syncPref = 3;
        }
        cancelAlarm(0);  // legacy
        cancelAlarm(1);  // legacy
        cancelAlarm(2);  // legacy
        cancelAlarm(4);  // 4:30 am
        cancelAlarm(8);  // 8:30 am
        cancelAlarm(13); // 1:30 pm
        cancelAlarm(24); // hourly
        cancelAlarm(10); //for testing only

        Random r = new Random();
        int val = r.nextInt(15); //between 14 and 0;
        int sign = ~(r.nextInt(2));
        // jitter makes sure that there is randomness to the downloads,
        // so everyone doesn't try to download at the same time
        int jitter = sign * val;

        //Alarm 4 - 4 am --- 3 a day plan
        if ((syncPref == 3)) {
            setAlarm(4, 4, jitter, AlarmManager.INTERVAL_DAY);
        }

        //Alarm 8 - 8:30 am --- 3 a day plan or daily plan
        if ((syncPref == 3) || (syncPref == 1)) {
            setAlarm(8, 8, 30 + jitter, AlarmManager.INTERVAL_DAY);
        }

        //Alarm 13 - 1:30 pm --- 3 a day plan
        if (syncPref == 3) {
            setAlarm(13, 13, 30 + jitter, AlarmManager.INTERVAL_DAY);
        }

        //Alarm 24 -- hourly plan
        if (syncPref == 24) {
            setAlarm(24, 0, 0, AlarmManager.INTERVAL_HOUR);
        }

        //Alarm 10 - Testing only
        //This is every 30 seconds! Disable before publishing!
        //setTestAlarm();
    }

    private void setAlarm(int alarm, int hour, int minute, long interval) {

        Date now = new Date();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);
        int nowHour = nowCal.get(Calendar.HOUR_OF_DAY);
        int nowMinute = nowCal.get(Calendar.MINUTE);

        //set time as indicated
        nowCal.set(Calendar.HOUR_OF_DAY, hour);
        nowCal.set(Calendar.MINUTE, minute);

        // if alarm is set for Hourly, set for an hour from now instead
        if (interval == AlarmManager.INTERVAL_HOUR) {
            nowCal.set(Calendar.HOUR_OF_DAY, nowHour + 1);
            nowCal.set(Calendar.MINUTE, nowMinute);
        }
        // if alarm time already occurred today, set the day for tomorrow
        else if ((nowHour > hour) || ((nowHour == hour) && (nowMinute >= minute))) {
            nowCal.set(Calendar.DATE, nowCal.get(Calendar.DATE) + 1);
        }

        //set the alarm
        AlarmManager alarmMgr;
        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
        intent.putExtra("refreshSource", ArticleParser.SourceMode.PREFER_DOWNLOAD);
        intent.putExtra("alarm", "alarm-" + alarm);
        intent.putExtra("getImages", "ALLOW_BOTH");
        intent.putExtra("dataSources", "news");

        PendingIntent alarmIntent = PendingIntent.getService(getApplicationContext(), alarm, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC, nowCal.getTimeInMillis(),
                interval, alarmIntent);

        //report alarm set to console
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, hh:mm a");
        Date setFor = nowCal.getTime();
        String setString = sdf.format(setFor);
        Log.d("ArticleDownloaderService", "Alarm " + alarm + " set for " + setString);
    }

    // clear alarms before setting (just in case)
    private void cancelAlarm(int alarm) {
        AlarmManager alarmMgr;
        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
        PendingIntent alarmIntent = PendingIntent.getService(getApplicationContext(), alarm, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.cancel(alarmIntent);
    }

    /*
     * Send a "New news item downloaded" notification
     */
    private void sendNotification() {

        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String nottest = prefs.getString("notifications_new_message_ringtone","not found");
        String notifications_new_message_ringtone = prefs.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound");
        Boolean notifications_new_message_vibrate = prefs.getBoolean("notifications_new_message_vibrate", true);

        List<Article> articleList = newsDataSource.getAllArticles();
        if ((articleList != null) && articleList.size() > 0) {

            Article article = articleList.get(0);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.hhs_white)
                            //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_hhs_hollow))
                    .setContentTitle("News from Holliston High")
                    .setContentText(article.title);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("fromNotification", true);
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());

            if (!notifications_new_message_ringtone.equals("silent")) {
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    ringtone.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (notifications_new_message_vibrate) {
                try {
                    Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    v.vibrate(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Debugging method only
    private void setTestAlarm() {
        Date now = new Date();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);
        nowCal.set(Calendar.SECOND, nowCal.get(Calendar.SECOND)+30);

        AlarmManager alarmMgr;
        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
        intent.putExtra("refreshSource", ArticleParser.SourceMode.DOWNLOAD_ONLY);
        intent.putExtra("alarm", "alarm-"+10);

        PendingIntent alarmIntent = PendingIntent.getService(getApplicationContext(), 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC, nowCal.getTimeInMillis(),
                1000*30, alarmIntent);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, hh:mm:ss a");
        Date setFor = nowCal.getTime();
        String setString = sdf.format(setFor);
        Log.d("ArticleDownloaderService", "TestAlarm(10) set for "+setString);
    }

    /*
     * Custom asyncTask to download data for a single datasource
     */
    private class refreshDataSource extends AsyncTask<Void, Void, String> {

        final ArticleDataSource dataSource;
        final Boolean triggersNotification;
        final Boolean cacheImages;

        public refreshDataSource(ArticleDataSource dataSource, Boolean cacheImages, Boolean triggersNotification) {
            this.dataSource = dataSource;
            this.cacheImages = cacheImages;
            this.triggersNotification = triggersNotification;
        }

        @Override
        protected String doInBackground(Void... arg0) {

            String result;
            try {
                String mostRecentNewsKey = "";

                // if this source can trigger a notification,
                // record the first article in this datasource
                if (triggersNotification) {
                    List<Article> newsList = dataSource.getAllArticles();
                    mostRecentNewsKey = "";
                    if ((newsList != null) && (newsList.size() > 0)) {
                        mostRecentNewsKey = newsList.get(0).title;
                    }
                }

                // request data download, retrieve string indicating success or not
                result = dataSource.downloadArticles(ArticleParser.SourceMode.PREFER_DOWNLOAD);
                Log.i("ArticleDownloaderService", dataSource.getName() + ": " + result);

                // if this datasource can trigger a notification,
                // compare new article(0) with the mostRecent article.
                // alarmSource != null means that this was called by the
                // user from Main Activity, so there's not need for a notification
                if (triggersNotification && (alarmSource != null)) {
                    List<Article> downloadedList = dataSource.getAllArticles();
                    if ((downloadedList != null) && (downloadedList.size() > 0)) {
                        String downloadedNewsKey = downloadedList.get(0).title;
                        if (!downloadedNewsKey.equals(mostRecentNewsKey)) {
                            sendNotification();
                        }
                    }
                    //comment out the next line (it forces a notification)
                    //sendNotification();
                }
            } catch (Exception e) {
                result = getResources().getString(R.string.xml_error);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            SharedPreferences prefs = getSharedPreferences("hhsapp", 0);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putLong(this.dataSource.getName(), new Date().hashCode());
            editor.apply();

            publishResults(this.dataSource.getName(), result);
            // if requested, re-download and cache all images
            if (this.cacheImages) {
                //dataSource.downloadAndCacheImages();
                //this might be a data killer
            }
        }
    }
}


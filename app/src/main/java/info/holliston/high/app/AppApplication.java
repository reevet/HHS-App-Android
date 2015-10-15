package info.holliston.high.app;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by reevet on 3/27/2015.
 */
public class AppApplication extends Application {

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // from tutorial: Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // from tutorial: Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = analytics.newTracker(getResources().getString(R.string.google_analytics_id));
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
}

package info.holliston.high.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.ArticleWarehouse;
import info.holliston.high.app.datamodel.download.ArticleDownloaderService;
import info.holliston.high.app.datamodel.download.ArticleParser;
import info.holliston.high.app.datamodel.download.ArticleSQLiteHelper;
import info.holliston.high.app.list.DailyAnnListFragment;
import info.holliston.high.app.list.EventsListFragment;
import info.holliston.high.app.list.LunchListFragment;
import info.holliston.high.app.list.NewsRecyclerFragment;
import info.holliston.high.app.list.SchedulesListFragment;
import info.holliston.high.app.navdrawer.NavDrawerItem;
import info.holliston.high.app.navdrawer.NavDrawerListAdapter;
import info.holliston.high.app.pager.MainPagerFragment;
import info.holliston.high.app.pager.NewsPagerFragment;
import info.holliston.high.app.pager.adapter.MainPagerAdapter;
import info.holliston.high.app.widget.HHSWidget;

public class MainActivity extends AppCompatActivity {

    private static int sCurrentNewsItem = -1;
    //datasources
    public ArticleWarehouse warehouse;
    //Paging and Adapters
    private MainPagerFragment sMainPagerFragment;
    private ViewPager sViewPager;
    //main category fragments
    private HomeFragment sHomeFragment;
    private DailyAnnListFragment sDailyAnnFragment;
    private EventsListFragment sEventsFragment;
    private LunchListFragment sLunchFragment;
    private NewsRecyclerFragment sNewsFragment;
    private SchedulesListFragment sSchedFragment;
    private SocialFragment sSocialFragment;
    //variables
    private Boolean sNewNewsAvailable = false;

    private int sCurrentView = -1;
    //Menu drawer and action bar
    private DrawerLayout sDrawerLayout;
    private int sIsRefreshing = 0;
    /*
     * receive messages for data refresh completion or notification
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String result = bundle.getString("result");
                String name = bundle.getString("datasource");

                //update sections of UI based on which feeds report updated
                if ((result != null) && (name != null)) {
                    switch (name) {
                        case ArticleSQLiteHelper.TABLE_SCHEDULES:
                            sSchedFragment.setCurrentArticle(-1);
                            sSchedFragment.updateUI();
                            sHomeFragment.updateSchedulesUI();
                            break;
                        case ArticleSQLiteHelper.TABLE_DAILYANN:
                            sDailyAnnFragment.setCurrentArticle(-1);
                            sDailyAnnFragment.updateUI();
                            sHomeFragment.updateDailyAnnUI();
                            break;
                        case ArticleSQLiteHelper.TABLE_NEWS:
                            sNewsFragment.setCurrentArticle(-1);
                            sNewsFragment.updateUI();
                            sHomeFragment.updateNewsUI();
                            break;
                        case ArticleSQLiteHelper.TABLE_EVENTS:
                            sEventsFragment.setCurrentArticle(-1);
                            sEventsFragment.updateUI();
                            sHomeFragment.updateEventsUI();
                            break;
                        case ArticleSQLiteHelper.TABLE_LUNCH:
                            sLunchFragment.setCurrentArticle(-1);
                            sLunchFragment.updateUI();
                            sHomeFragment.updateLunchUI();
                            break;
                    }
                    sIsRefreshing++;

                    //when all 5 report in, turn off SwipeRefresh animation
                    if (sIsRefreshing == 5) {
                        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_container);
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                            sIsRefreshing = 0;
                            SharedPreferences prefs = getSharedPreferences("hhsapp", 0);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("firstTime", false);
                            editor.apply();
                        }
                    }
                }
            }
        }
    };
    private Intent sShareIntent;
    private LinearLayout mDrawerLinLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If a notification waa clicked to open this activity, note it here so that
        //we can redirect to the news page later
        Intent intent = getIntent();
        Boolean notification = intent.getBooleanExtra("fromNotification", false);
        if (notification) {
            sNewNewsAvailable = true;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(0);
            }
        } else {
            sNewNewsAvailable = false;
        }
        //sNewNewsAvailable = true;  //for debug

        //set up the menu drawer
        setUpMenuDrawer();

        // enabling action bar app icon to behave as toggle button
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
            bar.setIcon(R.drawable.ic_hhs_hollow);
        }

        //set up datasources and category fragments
        warehouse = new ArticleWarehouse(getApplicationContext());
        sHomeFragment = new HomeFragment();
        sDailyAnnFragment = new DailyAnnListFragment();
        sEventsFragment = new EventsListFragment();
        sLunchFragment = new LunchListFragment();
        sNewsFragment = new NewsRecyclerFragment();
        sSchedFragment = new SchedulesListFragment();
        sSocialFragment = new SocialFragment();

        //set up the main category pager
        startPager();

        //suggest rating this app
        AppRater.app_launched(this);

        //set alarms to download data
        setDownloadAlarms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check if this is the first time the app is launched
        checkIfFirstTime();

        refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD);

        //prepare to listen for notifications
        registerReceiver(receiver, new IntentFilter(ArticleDownloaderService.APP_RECEIVER));
        registerReceiver(receiver, new IntentFilter(HHSWidget.NOTIFICATION));

        // Get tracker.
        Tracker t = ((AppApplication) getApplication()).getTracker(
                AppApplication.TrackerName.APP_TRACKER);

        // Set screen name.
        t.setScreenName("MainActivity");

        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
        Log.i("MainActivity", "Analytics tracker sent");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("sCurrentView", sCurrentView);
        getSupportFragmentManager()
                .putFragment(outState, HomeFragment.class.getName(), sHomeFragment);
        getSupportFragmentManager()
                .putFragment(outState, SchedulesListFragment.class.getName(), sSchedFragment);
        getSupportFragmentManager()
                .putFragment(outState, NewsRecyclerFragment.class.getName(), sNewsFragment);
        getSupportFragmentManager()
                .putFragment(outState, DailyAnnListFragment.class.getName(), sDailyAnnFragment);
        getSupportFragmentManager()
                .putFragment(outState, EventsListFragment.class.getName(), sEventsFragment);
        getSupportFragmentManager()
                .putFragment(outState, LunchListFragment.class.getName(), sLunchFragment);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        sCurrentView = savedInstanceState.getInt("sCurrentView");
        sHomeFragment = (HomeFragment) getSupportFragmentManager()
                .getFragment(savedInstanceState, HomeFragment.class.getName());
        sSchedFragment = (SchedulesListFragment) getSupportFragmentManager()
                .getFragment(savedInstanceState, SchedulesListFragment.class.getName());
        sNewsFragment = (NewsRecyclerFragment) getSupportFragmentManager()
                .getFragment(savedInstanceState, NewsRecyclerFragment.class.getName());
        sDailyAnnFragment = (DailyAnnListFragment) getSupportFragmentManager().getFragment(
                savedInstanceState, DailyAnnListFragment.class.getName());
        sEventsFragment = (EventsListFragment) getSupportFragmentManager().getFragment(
                savedInstanceState, EventsListFragment.class.getName());
        sLunchFragment = (LunchListFragment) getSupportFragmentManager().getFragment(
                savedInstanceState, LunchListFragment.class.getName());

        super.onRestoreInstanceState(savedInstanceState);
    }

    public void refreshData(ArticleParser.SourceMode refreshSource) {

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_container);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        Toast.makeText(this, "Loading data", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, ArticleDownloaderService.class);
        if (refreshSource == null) {
            refreshSource = ArticleParser.SourceMode.PREFER_DOWNLOAD;
        }

        intent.putExtra("datasources", "all");

        intent.putExtra("refreshSource", refreshSource);
        intent.putExtra("getImages", "DOWNLOAD_ONLY");

        startService(intent);
        Log.d("MainActivity", "Refresh intent sent to ArticleDownloaderService");

        displayView(0);
        getDrawerLayout().closeDrawers();
    }

    public void setDownloadAlarms() {
        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
        intent.putExtra("alarmReset", "reset");
        startService(intent);
    }

    /*
     * Prepares the main category pager
     */
    private void startPager() {
        //create main pager and attach adapter
        sMainPagerFragment = new MainPagerFragment();
        MainPagerAdapter sMainPagerAdapter = new MainPagerAdapter(this, getSupportFragmentManager());
        sViewPager = findViewById(R.id.frame_pager);
        if (sViewPager != null) {
            sViewPager.addOnPageChangeListener(sMainPagerFragment);
            sViewPager.setAdapter(sMainPagerAdapter);
            sViewPager.setOffscreenPageLimit(6);
        }

        //set up one-pane or two-pane layouts
        if (findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, sMainPagerFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            NewsPagerFragment newsPager = new NewsPagerFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", 0);  //shows first news article
            newsPager.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_list_container, sMainPagerFragment);
            transaction.replace(R.id.frame_detail_container, newsPager);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    /**
     * Displaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // update the main content by replacing fragments
        if (position < 0) {
            position = 0;
        }

        // if option 6 (Sports)
        if (position == 6) {
            Uri uriUrl = Uri.parse(getResources().getString(R.string.hhs_sports_page));
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
            return;
        }

        // if option 8 (HHS Webpage)
        if (position == 8) {
            Uri uriUrl = Uri.parse(getResources().getString(R.string.hhs_home_page));
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
            return;
        }
        // if option 9 (refresh data)
        else if (position == 9) {
            refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD);
            return;
        }

        //if in detail mode
        if (findViewById(R.id.detail_pager) != null) {
            if (findViewById(R.id.frame_container) != null) {
                getSupportFragmentManager().popBackStack();
                sMainPagerFragment.setPage(position);
                sCurrentView = position;
            } else {
                sMainPagerFragment.setPage(position);
                sCurrentView = position;
            }
        } else {
            sMainPagerFragment.setPage(position);
            sCurrentView = position;
        }

        //if the drawer is open, close it.
        DrawerLayout mDrawerLayout;
        mDrawerLayout = findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if ((findViewById(R.id.frame_detail_container) != null) //landscape mode
                || (findViewById(R.id.detail_pager) == null)) { //not showing detail portrait
            if (findViewById(R.id.settings_layout) != null) {
                super.onBackPressed();
            } else if (sCurrentView != 0) {
                sMainPagerFragment.setPage(0);
            }
        } else {
            super.onBackPressed(); // This will pop the Activity from the stack.
        }
        sCurrentNewsItem = -1;
        refreshActionBar(this);
    }

    /*
     * Check for first launch, to download all data
     */
    private void checkIfFirstTime() {
        SharedPreferences prefs = getSharedPreferences("hhsapp", 0);
        Boolean firstTime = prefs.getBoolean("firstTime", true);

        if (firstTime) {
            refreshData(ArticleParser.SourceMode.DOWNLOAD_ONLY);
        }
    }

    /*
     * Drawer menu setup
     */
    private void setUpMenuDrawer() {
        // load slide menu items
        String[] navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray navMenuIcons;
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        ArrayList<NavDrawerItem> navDrawerItems;

        //get drawer elements
        sDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLinLayout = findViewById(R.id.drawer_lin_layout);
        ListView mDrawerList = findViewById(R.id.list_slidermenu);

        // adding nav drawer items to array
        navDrawerItems = new ArrayList<>();
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1))); //Home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1))); //Schedules
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1))); //News
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1))); //DailyAnn
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1))); //Events
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1))); //Lunch
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1))); //Sports
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1))); //Social Media
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(8, -1))); //Website
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[9], navMenuIcons.getResourceId(9, -1))); //Refresh

        // Recycle the typed array
        navMenuIcons.recycle();
        // setting the nav drawer list adapter

        NavDrawerListAdapter adapter;
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);

        if (mDrawerList != null) {
            mDrawerList.setAdapter(adapter);
            mDrawerList.setOnItemClickListener(new SlideMenuClickListener()); //custom object is defined below
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, sDrawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                invalidateOptionsMenu();
            }
        };
        sDrawerLayout.setDrawerListener(mDrawerToggle);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setsShareIntent();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD);
                return true;
            case R.id.action_settings:
                SettingsFragment settingsFragment = new SettingsFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack(null);

                if (findViewById(R.id.frame_detail_container) == null) {
                    transaction.replace(R.id.frame_container, settingsFragment);
                } else {
                    transaction.replace(R.id.frame_detail_container, settingsFragment);
                }
                transaction.commit();
                return false;
            case R.id.menu_item_share:
                if ((mShareActionProvider != null) && (sShareIntent != null)) {
                    mShareActionProvider.setShareIntent(sShareIntent);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* *
     * Called when invalidateOptionsMenu() is triggered
	 */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = sDrawerLayout.isDrawerOpen(mDrawerLinLayout);
        boolean showingNewsDetail = sCurrentNewsItem >= 0;
        menu.findItem(R.id.menu_item_share).setVisible(!drawerOpen && showingNewsDetail);
        menu.findItem(R.id.action_refresh).setVisible(!drawerOpen);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    void setsShareIntent() {
        String shareText;
        if (getsCurrentNewsItem() >= 0) {
            Article article = warehouse.getAllArticles(ArticleWarehouse.StoreType.NEWS).get(getsCurrentNewsItem());
            String url = article.url.toString();
            url = url.replace("https://sites.google.com/a/holliston.k12.ma.us/holliston-high-school/", "http://hhs.holliston.k12.ma.us/");
            shareText = article.title + " " + url;
        } else {
            shareText = "http://hhs.holliston.k12.ma.us";
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.setType("text/plain");
        sShareIntent = shareIntent;
        mShareActionProvider.setShareIntent(shareIntent);
    }

    public void refreshActionBar(Activity a) {
        a.invalidateOptionsMenu();
    }

    public MainPagerFragment getsMainPagerFragment() {
        return sMainPagerFragment;
    }

    public ViewPager getsViewPager() {
        return sViewPager;
    }

    public ArticleWarehouse getWarehouse() {
        return warehouse;
    }

    public HomeFragment getsHomeFragment() {
        return sHomeFragment;
    }

    public DailyAnnListFragment getsDailyAnnFragment() {
        return sDailyAnnFragment;
    }

    public EventsListFragment getsEventsFragment() {
        return sEventsFragment;
    }

    public LunchListFragment getsLunchFragment() {
        return sLunchFragment;
    }

    public NewsRecyclerFragment getsNewsFragment() {
        return sNewsFragment;
    }

    public SchedulesListFragment getsSchedFragment() {
        return sSchedFragment;
    }

    public SocialFragment getsSocialFragment() {
        return sSocialFragment;
    }

    private int getsCurrentNewsItem() {
        return sCurrentNewsItem;
    }

    public void setsCurrentNewsItem(int sCurrentNewsItem) {
        MainActivity.sCurrentNewsItem = sCurrentNewsItem;
    }

    public Boolean getsNewNewsAvailable() {
        return sNewNewsAvailable;
    }

    public void setsNewNewsAvailable(Boolean bool) {
        sNewNewsAvailable = bool;
    }

    public void setsCurrentView(int sCurrentView) {
        sCurrentView = sCurrentView;
    }

    private DrawerLayout getDrawerLayout() {
        return sDrawerLayout;
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

}

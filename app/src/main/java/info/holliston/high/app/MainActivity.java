package info.holliston.high.app;

import android.app.ActionBar;
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
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import info.holliston.high.app.datamodel.download.ArticleDataSource;
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
import info.holliston.high.app.pager.NewsPagerFragment;
import info.holliston.high.app.pager.TabPagerFragment;
import info.holliston.high.app.pager.adapter.TabPagerAdapter;
import info.holliston.high.app.widget.HHSWidget;

public class MainActivity extends FragmentActivity {

    //Paging and Adapters
    public TabPagerFragment tabPagerFragment;
    public TabPagerAdapter tabPagerAdapter;
    public ViewPager mViewPager;
    //datasources
    public ArticleDataSource scheduleSource;
    public ArticleDataSource newsSource;
    public ArticleDataSource dailyannSource;
    public ArticleDataSource eventsSource;
    public ArticleDataSource lunchSource;
    //main category fragments
    public HomeFragment mHomeFragment;
    public DailyAnnListFragment mDailyAnnFragment;
    public EventsListFragment mEventsFragment;
    public LunchListFragment mLunchFragment;
    public NewsRecyclerFragment mNewsFragment;
    public SchedulesListFragment mSchedFragment;
    //variables
    public Boolean newNewsAvailable = false;
    public int currentView = -1;
    //Menu drawer and action bar
    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerLinLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int isRefreshing = 0;
    /*
     * receive messages for data refresh completion or notification
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {

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
                            mSchedFragment.updateUI();
                            mHomeFragment.updateSchedulesUI();
                            break;
                        case ArticleSQLiteHelper.TABLE_DAILYANN:
                            mDailyAnnFragment.updateUI();
                            mHomeFragment.updateDailyAnnUI();
                            break;
                        case ArticleSQLiteHelper.TABLE_NEWS:
                            mNewsFragment.updateUI();
                            mHomeFragment.updateNewsUI();
                            break;
                        case ArticleSQLiteHelper.TABLE_EVENTS:
                            mEventsFragment.updateUI();
                            mHomeFragment.updateEventsUI();
                            break;
                        case ArticleSQLiteHelper.TABLE_LUNCH:
                            mLunchFragment.updateUI();
                            mHomeFragment.updateLunchUI();
                            break;
                    }
                    isRefreshing++;

                    //when all 5 report in, turn off SwipeRefresh animation
                    if (isRefreshing == 5) {
                        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                            isRefreshing = 0;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If a notification waa clicked to open this activity, note it here so that
        //we can redirect to the news page later
        Intent intent = getIntent();
        Boolean notification = intent.getBooleanExtra("fromNotification", false);
        if (notification) {
            newNewsAvailable = true;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
        } else {
            newNewsAvailable = false;
        }
        //newNewsAvailable = true;  //for debug

        //set up the menu drawer
        setUpMenuDrawer();

        // enabling action bar app icon to behave as toggle button
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
            bar.setIcon(R.drawable.ic_hhs_hollow);
        }

        //set up datasources and category fragments
        defineDataSources();
        mHomeFragment = new HomeFragment();
        mDailyAnnFragment = new DailyAnnListFragment();
        mEventsFragment = new EventsListFragment();
        mLunchFragment = new LunchListFragment();
        mNewsFragment = new NewsRecyclerFragment();
        mSchedFragment = new SchedulesListFragment();

        //set up the main category pager
        startPager();

        //suggest rating this app
        AppRater.app_launched(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check if this is the first time the app is launched
        checkIfFirstTime();

        //prepare to listen for notifications
        registerReceiver(receiver, new IntentFilter(ArticleDownloaderService.APP_RECEIVER));
        registerReceiver(receiver, new IntentFilter(HHSWidget.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentView", currentView);
        getSupportFragmentManager()
                .putFragment(outState, HomeFragment.class.getName(), mHomeFragment);
        getSupportFragmentManager()
                .putFragment(outState, SchedulesListFragment.class.getName(), mSchedFragment);
        getSupportFragmentManager()
                .putFragment(outState, NewsRecyclerFragment.class.getName(), mNewsFragment);
        getSupportFragmentManager()
                .putFragment(outState, DailyAnnListFragment.class.getName(), mDailyAnnFragment);
        getSupportFragmentManager()
                .putFragment(outState, EventsListFragment.class.getName(), mEventsFragment);
        getSupportFragmentManager()
                .putFragment(outState, LunchListFragment.class.getName(), mLunchFragment);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        currentView = savedInstanceState.getInt("currentView");
        mHomeFragment = (HomeFragment) getSupportFragmentManager()
                .getFragment(savedInstanceState, HomeFragment.class.getName());
        mSchedFragment = (SchedulesListFragment) getSupportFragmentManager()
                .getFragment(savedInstanceState, SchedulesListFragment.class.getName());
        mNewsFragment = (NewsRecyclerFragment) getSupportFragmentManager()
                .getFragment(savedInstanceState, NewsRecyclerFragment.class.getName());
        mDailyAnnFragment = (DailyAnnListFragment) getSupportFragmentManager().getFragment(
                savedInstanceState, DailyAnnListFragment.class.getName());
        mEventsFragment = (EventsListFragment) getSupportFragmentManager().getFragment(
                savedInstanceState, EventsListFragment.class.getName());
        mLunchFragment = (LunchListFragment) getSupportFragmentManager().getFragment(
                savedInstanceState, LunchListFragment.class.getName());

        super.onRestoreInstanceState(savedInstanceState);

    }

    /*
     * Prepares the main category pager
     */
    private void startPager() {
        //create main pager and attach adapter
        tabPagerFragment = new TabPagerFragment();
        tabPagerAdapter = new TabPagerAdapter(this, getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.frame_pager);
        mViewPager.setOnPageChangeListener(tabPagerFragment);
        mViewPager.setAdapter(tabPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);

        //set up one-pane or two-pane layouts
        if (findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, tabPagerFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            NewsPagerFragment newsPager = new NewsPagerFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", 0);  //shows first news article
            newsPager.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_list_container, tabPagerFragment);
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

        // if option 6 (HHS Webpage)
        if (position == 6) {
            Uri uriUrl = Uri.parse(getResources().getString(R.string.hhs_home_page));
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
            return;
        }
        // if option 7 (refresh data)
        else if (position == 7) {
            refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD, true);
            return;
        }

        //if in detail mode
        if (findViewById(R.id.detail_pager) != null) {
            if (findViewById(R.id.frame_container) != null) {
                getSupportFragmentManager().popBackStack();
                tabPagerFragment.setPage(position);
                currentView = position;
            } else {
                tabPagerFragment.setPage(position);
                currentView = position;
            }
        } else {
            tabPagerFragment.setPage(position);
            currentView = position;
        }

        //if the drawer is open, close it.
        DrawerLayout mDrawerLayout;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
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

    public void refreshData(ArticleParser.SourceMode refreshSource, Boolean cacheImages) {

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        Toast.makeText(MainActivity.this, "Loading data", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
        if (refreshSource == null) {
            refreshSource = ArticleParser.SourceMode.PREFER_DOWNLOAD;
        }

        intent.putExtra("refreshSource", refreshSource);
        if (cacheImages) {
            intent.putExtra("getImages", "DOWNLOAD_ONLY");
        }

        startService(intent);
        Log.d("MainActivity", "Refresh intent sent to ArticleDownloaderService");

        displayView(0);
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onBackPressed() {
        if ((findViewById(R.id.frame_detail_container) != null) //landscape mode
                || (findViewById(R.id.detail_pager) == null)) { //not showing detail portrait
            if (findViewById(R.id.settings_layout) != null) {
                super.onBackPressed();
            } else if (currentView != 0) {
                tabPagerFragment.setPage(0);
            }
        } else {
            super.onBackPressed(); // This will pop the Activity from the stack.
        }
    }

    /*
     * Define the datasources for each feed
     */
    private void defineDataSources() {
        ArticleDataSource.ArticleDataSourceOptions options;

        options = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_NEWS,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.XML,
                getString(R.string.news_url),
                getResources().getStringArray(R.array.news_parser_names),
                ArticleParser.HtmlTags.KEEP_HTML_TAGS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_PAST,
                "1");
        newsSource = new ArticleDataSource(getApplicationContext(), options);

        options = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_SCHEDULES,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.JSON,
                getString(R.string.schedules_url),
                getResources().getStringArray(R.array.schedules_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "2");
        scheduleSource = new ArticleDataSource(getApplicationContext(), options);

        options = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_LUNCH,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.JSON,
                getString(R.string.lunch_url),
                getResources().getStringArray(R.array.lunch_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "5");
        lunchSource = new ArticleDataSource(getApplicationContext(), options);

        options = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_DAILYANN,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.XML,
                getString(R.string.dailyann_url),
                getResources().getStringArray(R.array.dailyann_parser_names),
                ArticleParser.HtmlTags.CONVERT_LINE_BREAKS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_PAST,
                "1");
        dailyannSource = new ArticleDataSource(getApplicationContext(), options);

        options = new ArticleDataSource.ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_EVENTS,
                ArticleDataSource.ArticleDataSourceOptions.SourceType.JSON,
                getString(R.string.events_url),
                getResources().getStringArray(R.array.events_parser_names),
                ArticleParser.HtmlTags.CONVERT_LINE_BREAKS,
                ArticleDataSource.ArticleDataSourceOptions.SortOrder.GET_FUTURE,
                "20");
        eventsSource = new ArticleDataSource(getApplicationContext(), options);
    }

    /*
     * Check for first launch, to download all data
     */
    private void checkIfFirstTime() {
        SharedPreferences prefs = getSharedPreferences("hhsapp", 0);
        Boolean firstTime = prefs.getBoolean("firstTime", true);

        if (firstTime) {
            refreshData(ArticleParser.SourceMode.DOWNLOAD_ONLY, true);
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
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLinLayout = (LinearLayout) findViewById(R.id.drawer_lin_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        // adding nav drawer items to array
        navDrawerItems = new ArrayList<>();
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1))); //Home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1))); //Schedules
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1))); //News
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1))); //DailyAnn
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1))); //Events
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1))); //Lunch
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1))); //Website
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1))); //Refresh

        // Recycle the typed array
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener()); //custom object is defined below

        // setting the nav drawer list adapter
        NavDrawerListAdapter adapter;
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
                refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD, true);
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
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerLinLayout);
        menu.findItem(R.id.action_refresh).setVisible(!drawerOpen);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
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

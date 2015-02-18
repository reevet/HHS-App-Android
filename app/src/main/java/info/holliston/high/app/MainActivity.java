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

    final int HOURS_TO_AUTOREFRESH = 12;
    public TabPagerFragment tabPagerFragment;
    public TabPagerAdapter tabPagerAdapter;
    public ViewPager mViewPager;

    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerLinLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    // nav drawer title
    //private CharSequence mDrawerTitle;
    //private int mDrawerIcon;

    public ArticleDataSource scheduleSource;
    public ArticleDataSource newsSource;
    public ArticleDataSource dailyannSource;
    public ArticleDataSource eventsSource;
    public ArticleDataSource lunchSource;

    public HomeFragment mHomeFragment;
    public DailyAnnListFragment mDailyAnnFragment;
    public EventsListFragment mEventsFragment;
    public LunchListFragment mLunchFragment;
    public NewsRecyclerFragment mNewsFragment;
    public SchedulesListFragment mSchedFragment;

    public Boolean newNewsAvailable = false;
    private int isRefreshing = 0;

    // used to store app title
    //private CharSequence mTitle;
    //private int mIcon;

    public int currentView = -1;

    ArticleParser.SourceMode refreshSource;

    //receive messages about data download
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String result = bundle.getString("result");
                String name = bundle.getString("datasource");
                String notification = bundle.getString("fromNotification");
                if ((result != null) && (name != null)) {
                    if (name.equals(ArticleSQLiteHelper.TABLE_SCHEDULES)) {
                        mSchedFragment.updateUI();
                        mHomeFragment.updateSchedulesUI();
                        isRefreshing++;
                    } else if (name.equals(ArticleSQLiteHelper.TABLE_DAILYANN)) {
                        mDailyAnnFragment.updateUI();
                        mHomeFragment.updateDailyAnnUI();
                        isRefreshing++;
                    } else if (name.equals(ArticleSQLiteHelper.TABLE_NEWS)) {
                        mNewsFragment.updateUI();
                        mHomeFragment.updateNewsUI();
                        isRefreshing++;
                    }else if (name.equals(ArticleSQLiteHelper.TABLE_EVENTS)) {
                        mEventsFragment.updateUI();
                        mHomeFragment.updateEventsUI();
                        isRefreshing++;
                    }else if (name.equals(ArticleSQLiteHelper.TABLE_LUNCH)) {
                        mLunchFragment.updateUI();
                        mHomeFragment.updateLunchUI();
                        isRefreshing++;
                    }
                   if (isRefreshing == 5) {
                       SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                            isRefreshing = 0;
                            SharedPreferences prefs = getSharedPreferences("hhsapp", 0);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("firstTime", false);
                            editor.commit();
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

        Intent intent = getIntent();
        Boolean notification = intent.getBooleanExtra("fromNotification", false);
        if (notification) {
            newNewsAvailable = true;
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
        } else {
            newNewsAvailable = false;
        }
        //newNewsAvailable = true;  //for debug
        //title bar setup
        //mTitle = mDrawerTitle = getTitle();
        //mIcon = mDrawerIcon = R.drawable.ic_hhs_hollow;

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

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        //this is defined below

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

        // enabling action bar app icon and behaving it as toggle button
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
            bar.setIcon(R.drawable.ic_hhs_hollow);
        }
        defineDataSources();
        AppRater.app_launched(this);
        checkIfFirstTime();

        //decideIfRefresh();

        mHomeFragment = new HomeFragment();
        mDailyAnnFragment = new DailyAnnListFragment();
        mEventsFragment = new EventsListFragment();
        mLunchFragment = new LunchListFragment();
        mNewsFragment = new NewsRecyclerFragment();
        mSchedFragment = new SchedulesListFragment();

        startPager();
        }

    private void startPager() {

        TabPagerFragment tester = tabPagerFragment;
        tabPagerFragment = new TabPagerFragment();
        tabPagerAdapter =  new TabPagerAdapter(this, getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.frame_pager);
        mViewPager.setOnPageChangeListener(tabPagerFragment);
        mViewPager.setAdapter(tabPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);

        if (findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, tabPagerFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            NewsPagerFragment newsPager = new NewsPagerFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", 0);
            newsPager.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_list_container, tabPagerFragment);
            transaction.replace(R.id.frame_detail_container , newsPager);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(ArticleDownloaderService.NOTIFICATION));
        registerReceiver(receiver, new IntentFilter(HHSWidget.NOTIFICATION));

        SharedPreferences prefs = getSharedPreferences("hhsapp", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();

        Boolean data_changed = prefs.getBoolean("data_changed", false);
        if (data_changed) {
            editor.putBoolean("data_changed", false);
            editor.commit();
            Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(newIntent);
        }

        if (newNewsAvailable) {
            //displayView(2);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
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
                refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD, ImageAsyncCacher.SourceMode.DOWNLOAD_ONLY);
                return true;
            case R.id.action_settings:
                SettingsFragment settingsFragment = new SettingsFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.frame_container, settingsFragment);
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
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Displaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // update the main content by replacing fragments
        if (position <0) {
            position = 0;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);

        if (position == 6) {
            Uri uriUrl = Uri.parse(getResources().getString(R.string.hhs_home_page));
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
            return;
        } else if (position == 7) {
            refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD, ImageAsyncCacher.SourceMode.DOWNLOAD_ONLY);
            return;
        }


        //if in detail mode
        if (findViewById(R.id.detail_pager) != null) {
            if (findViewById(R.id.frame_container) != null) {
                getSupportFragmentManager().popBackStack();
                tabPagerFragment.setPage(position);
                //setActionBar(position);
                currentView = position;
            } else {
                tabPagerFragment.setPage(position);
                //setActionBar(position);
                currentView = position;
            }
        } else {
            tabPagerFragment.setPage(position);
            //setActionBar(position);
            currentView = position;
            //currentView = position;
        }

        DrawerLayout mDrawerLayout;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();


    }

    @Override
    public void setTitle(CharSequence title) {
        /*mTitle = title;
        ActionBar bar = getActionBar();
        if (bar != null) {
            getActionBar().setTitle(mTitle);
        }*/
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

    public void refreshData(ArticleParser.SourceMode refreshSource, ImageAsyncCacher.SourceMode getImages) {
        //destroyFragments();
        Log.d("MainActivity", "Refresh called");

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
        if (getImages == ImageAsyncCacher.SourceMode.ALLOW_BOTH) {
            intent.putExtra("getImages", "ALLOW_BOTH");
        } else if (getImages == ImageAsyncCacher.SourceMode.DOWNLOAD_ONLY) {
            intent.putExtra("getImages", "DOWNLOAD_ONLY");
        }

        startService(intent);

        Log.d("MainActivity", "Refresh intent sent to ArticleDownloaderService");

        mDrawerLayout.closeDrawers();
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

    /*private void setActionBar(int i) {
        TypedArray navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
        if (i == 0) {
            getActionBar().setTitle(R.string.app_name);
        } else {
            getActionBar().setTitle(navMenuTitles[i]);
        }
        getActionBar().setIcon(navMenuIcons.getResourceId(i,-1));

    }*/

    @Override
    public void onBackPressed() {
        if ((findViewById(R.id.frame_detail_container) != null) //landscape mode
                || (findViewById(R.id.detail_pager) == null)) { //not showing detail portrait
            if (findViewById(R.id.settings_layout) != null) {
                super.onBackPressed();
                Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
                intent.putExtra("alarmReset","reset");
                startService(intent);
            }else if (currentView != 0) {
                tabPagerFragment.setPage(0);
            }
        } else {
            super.onBackPressed(); // This will pop the Activity from the stack.
        }
    }



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

    /*private void decideIfRefresh() {
        SharedPreferences prefs = getSharedPreferences("hhsapp", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();

        Long date_lastupdate = prefs.getLong("date_lastupdate", 0);

        int updateLimit = HOURS_TO_AUTOREFRESH * 60 * 60 * 1000;
        //updateLimit = 1;
        Long timeSinceUpdate = System.currentTimeMillis()  - (date_lastupdate + updateLimit);

        if ((date_lastupdate == 0) ||  (timeSinceUpdate > 0) ) {
            updateAllUI();
            //refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD, ImageAsyncCacher.SourceMode.ALLOW_BOTH);
        } else {
             //for production
            //AppRater.showRateDialog(this, null); //for debug purposes
        }
    }*/
    private void checkIfFirstTime() {
        SharedPreferences prefs = getSharedPreferences("hhsapp", 0);
        Boolean firstTime = prefs.getBoolean("firstTime", true);

        if (firstTime) {
            refreshData(ArticleParser.SourceMode.DOWNLOAD_ONLY, ImageAsyncCacher.SourceMode.ALLOW_BOTH);
        }
    }
}

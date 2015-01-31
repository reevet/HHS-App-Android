package info.holliston.high.app;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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

import info.holliston.high.app.adapter.NavDrawerListAdapter;
import info.holliston.high.app.model.NavDrawerItem;
import info.holliston.high.app.pager.TabPager;
import info.holliston.high.app.widget.HHSWidget;
import info.holliston.high.app.xmlparser.ArticleParser;

public class MainActivity extends FragmentActivity {

    TabPager tabPager;

    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerLinLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

    String schedulesString;
    String eventsString;
    String newsString;
    String dailyAnnString;
    String lunchString;

	// nav drawer title
	private CharSequence mDrawerTitle;
    private int mDrawerIcon;
    //private ArrayList<NavDrawerItem> navDrawerItems;

    // used to store app title
	private CharSequence mTitle;
    private int mIcon;

    private CharSequence mLastTitle;
    private int mLastIcon;

	// slide menu items
	private String[] navMenuTitles;
	private ArrayList<String> fragmentData;


    public int currentView = -1;
    private int defaultView = 0;
    //Boolean newNewsAvailable = false;

    ArticleParser.SourceMode refreshSource;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                //Toast.makeText(MainActivity.this, "Download complete", Toast.LENGTH_LONG).show();
                //Toast.makeText(SplashActivity.this, "Data sync complete", Toast.LENGTH_LONG).show();
                String result = bundle.getString("result");
                if (result != null) {
                    SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_container);
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Download complete", Toast.LENGTH_LONG).show();
                    }
                }


                if (currentView >=0) {
                    displayView(currentView);
                } else {
                    displayView(defaultView);
                }

            }
        }
    };


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //layout
        mTitle = mDrawerTitle = getTitle();
        mIcon = mDrawerIcon = R.drawable.ic_hhs_hollow;

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        TypedArray navMenuIcons;
        // nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLinLayout = (LinearLayout) findViewById(R.id.drawer_lin_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        ArrayList<NavDrawerItem> navDrawerItems;
        fragmentData = new ArrayList<String>();

		// adding nav drawer items to array
		// Schedules

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // Home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        fragmentData.add("");
        // Schedules
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        fragmentData.add(schedulesString);
		// News
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        fragmentData.add(newsString);
        // Daily Announcements
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        fragmentData.add(dailyAnnString);
        // Events
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        fragmentData.add(eventsString);
        // Lunch
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        fragmentData.add(lunchString);
        // Website link
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        fragmentData.add("");
        // Refresh
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1)));
        fragmentData.add("");

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        NavDrawerListAdapter adapter;
		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		ActionBar bar = getActionBar();
        if (bar!=null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
            bar.setIcon(R.drawable.ic_hhs_hollow);
        }
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				//R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
		) {
			public void onDrawerClosed(View view) {
				//getActionBar().setTitle(mTitle);
                //getActionBar().setIcon(mIcon);
                //getActionBar().setIcon(navDrawerItems.get());
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				mLastTitle = getActionBar().getTitle();
                //mLastIcon = getActionBar().
                getActionBar().setTitle(mDrawerTitle);
                getActionBar().setIcon(mDrawerIcon);
                // calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

        /*schedulesFragment = new SchedulesListFragment();
        eventsFragment = new EventsListFragment();
        newsFragment = new NewsListFragment();
        dailyAnnFragment = new DailyAnnListFragment();
        */
        if (savedInstanceState == null) {
            //displayView(defaultView);
        }

        //SwipeRefresher Listener
        final SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {

                if (findViewById(R.id.detail_pager) == null) {
                    refreshData();
                } else
                    swipeRefreshLayout.setRefreshing(false);
            }
        });

        tabPager = new TabPager();

        if (findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, tabPager);
            transaction.addToBackStack(null);
            transaction.commit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Start download


        boolean isDebuggable =  ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );

        if ((currentView<0) && (isDebuggable == false)) {
            Toast.makeText(MainActivity.this, "Loading data", Toast.LENGTH_LONG).show();


            Intent i = getIntent();
            refreshSource = (ArticleParser.SourceMode) i.getSerializableExtra("refreshSource");
            if (refreshSource == null) {
                refreshSource = ArticleParser.SourceMode.PREFER_DOWNLOAD;
            }

            Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
            intent.putExtra("refreshSource", refreshSource);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //displayView(currentView);
        registerReceiver(receiver, new IntentFilter(ArticleDownloaderService.NOTIFICATION));
        registerReceiver(receiver, new IntentFilter(HHSWidget.NOTIFICATION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        //refreshData();
        super.onRestoreInstanceState(savedInstanceState);
        currentView = savedInstanceState.getInt("currentView");
    }

	/**
	 * Slide menu item click listener
	 * */
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
		case R.id.action_settings:
            refreshData();
			return true;
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
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Bundle bundle = new Bundle();
        bundle.putInt("position", position);


        if (position == 6) {
            Uri uriUrl = Uri.parse(getResources().getString(R.string.hhs_home_page));
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
            return;
        } else if (position == 7) {
            refreshData();
            return;
        }


        //if in detail mode
        if (findViewById(R.id.detail_pager) != null) {
            if (findViewById(R.id.frame_container) != null) {
                getSupportFragmentManager().popBackStack();
                tabPager.setPage(position);
                setActionBar(position);
                currentView=position;
            } else {
                TabPager newFragment = new TabPager();
                newFragment.setArguments(bundle);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_detail_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        } else {
            tabPager.setPage(position);
            setActionBar(position);
            currentView=position;
            //currentView = position;
        }
        if (position == 0) {
            SwipeRefreshLayout swipeLayout=(SwipeRefreshLayout) findViewById(R.id.swipe_container);
            swipeLayout.setEnabled(true);
        }

        DrawerLayout mDrawerLayout;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();


	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		ActionBar bar = getActionBar();
        if (bar !=null) {
            getActionBar().setTitle(mTitle);
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

    public void refreshData() {
        //destroyFragments();
        Log.d("MainActivity", "Refresh called");

        Toast.makeText(MainActivity.this, "Loading data", Toast.LENGTH_LONG).show();

        Intent i = getIntent();
        refreshSource = (ArticleParser.SourceMode) i.getSerializableExtra("refreshSource");
        if (refreshSource == null) {
            refreshSource = ArticleParser.SourceMode.PREFER_DOWNLOAD;
        }

        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
        intent.putExtra("refreshSource", refreshSource);
        startService(intent);

        Log.d("MainActivity", "Refresh intent sent to ArticleDownloaderService");

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentView", currentView);

    }

    private void setActionBar(int i) {
        /*TypedArray navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
        if (i == 0) {
            getActionBar().setTitle(R.string.app_name);
        } else {
            getActionBar().setTitle(navMenuTitles[i]);
        }
        getActionBar().setIcon(navMenuIcons.getResourceId(i,-1));
        */
    }

    @Override
    public void onBackPressed() {
        SwipeRefreshLayout swipeLayout=(SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setEnabled(true);
        super.onBackPressed();
    }
}

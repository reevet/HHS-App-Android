package info.holliston.high.app;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    // nav drawer title
    private CharSequence mDrawerTitle;
    private int mDrawerIcon;

    String schedulesString;
    String eventsString;
    String newsString;
    String dailyAnnString;
    String lunchString;

    // used to store app title
	private CharSequence mTitle;
    private int mIcon;

	// slide menu items
	private String[] navMenuTitles;
	private ArrayList<String> fragmentData;

    public int currentView = -1;

    ArticleParser.SourceMode refreshSource;

    //receive messages about data download
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String result = bundle.getString("result");
                if (result != null) {
                    SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_container);
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Download complete", Toast.LENGTH_LONG).show();
                        //Intent i = new Intent(MainActivity.this, MainActivity.class);
                        //startActivity(i);
                    }
                }
                displayView(0);
            }
        }
    };

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //title bar setup
        mTitle = mDrawerTitle = getTitle();
        mIcon = mDrawerIcon = R.drawable.ic_hhs_hollow;

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray navMenuIcons;
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        ArrayList<NavDrawerItem> navDrawerItems;

        //get drawer elements
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLinLayout = (LinearLayout) findViewById(R.id.drawer_lin_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        fragmentData = new ArrayList<String>();

		// adding nav drawer items to array
		navDrawerItems = new ArrayList<NavDrawerItem>();
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
        if (bar!=null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
            bar.setIcon(R.drawable.ic_hhs_hollow);
        }
        startPager();

    }

    private void startPager() {
        if (tabPager!=null) {
            tabPager.onDestroy();
        }
        tabPager = new TabPager();
        if (findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, tabPager);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_list_container, tabPager);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Start download

        boolean isDebuggable =  false;//( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );

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
            refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD);
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
            refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD);
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
                tabPager.setPage(position);
                setActionBar(position);
                currentView=position;
            }
        } else {
            tabPager.setPage(position);
            setActionBar(position);
            currentView=position;
            //currentView = position;
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

    public void refreshData(ArticleParser.SourceMode refreshSource) {
        //destroyFragments();
        Log.d("MainActivity", "Refresh called");

        Toast.makeText(MainActivity.this, "Loading data", Toast.LENGTH_LONG).show();

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


}

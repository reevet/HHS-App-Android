package info.holliston.high.app.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import info.holliston.high.app.DailyAnnListFragment;
import info.holliston.high.app.EventsListFragment;
import info.holliston.high.app.HomeFragment;
import info.holliston.high.app.LunchListFragment;
import info.holliston.high.app.NewsRecyclerFragment;
import info.holliston.high.app.SchedulesListFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
    public HomeFragment mHomeFragment;
    public SchedulesListFragment mSchedFragment;
    public NewsRecyclerFragment mNewsFragment;
    public DailyAnnListFragment mDailyAnnFragment;
    public EventsListFragment mEventsFragment;
    public LunchListFragment mLunchFragment;

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
        mHomeFragment = new HomeFragment();
        mSchedFragment = new SchedulesListFragment();
        mNewsFragment = new NewsRecyclerFragment();
        mDailyAnnFragment = new DailyAnnListFragment();
        mEventsFragment = new EventsListFragment();
        mLunchFragment = new LunchListFragment();
    }

    @Override
    public Fragment getItem(int i) {

        Fragment fragment = null;
        Boolean refresh = false;
        Bundle bundle = new Bundle();

        switch (i) {
            case 0:
                fragment = mHomeFragment;
                break;
            case 1:
                fragment = mSchedFragment;
                break;
            case 2:
                fragment = mNewsFragment;
                break;
            case 3:
                fragment = mDailyAnnFragment;
                break;
            case 4:
                fragment = mEventsFragment;
                break;
            case 5:
                fragment = mLunchFragment;
                break;
            /*case 6:
                Uri uriUrl = Uri.parse(getResources().getString(R.string.hhs_home_page));
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                return;
            case 7:
                refresh = true;
                break;
            */
            default:
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }


}

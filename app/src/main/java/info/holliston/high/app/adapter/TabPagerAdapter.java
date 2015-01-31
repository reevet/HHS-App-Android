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
    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        //Fragment fragment = new TabFragment();
        //Bundle args = new Bundle();
        // Our object is just an integer :-P
        //args.putInt(TabFragment.ARG_OBJECT, i + 1);
        //fragment.setArguments(args);

        //pasted in
        // update the main content by replacing fragments
        Fragment fragment = null;
        Boolean refresh = false;
        Bundle bundle = new Bundle();

        switch (i) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new SchedulesListFragment();
                 break;
            case 2:
                fragment = new NewsRecyclerFragment();
                break;
            case 3:
                fragment = new DailyAnnListFragment();
                break;
            case 4:
                fragment = new EventsListFragment();
                break;
            case 5:
                fragment = new LunchListFragment();
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

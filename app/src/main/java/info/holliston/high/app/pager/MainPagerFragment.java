package info.holliston.high.app.pager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.holliston.high.app.HomeFragment;
import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.SocialFragment;
import info.holliston.high.app.list.DailyAnnListFragment;
import info.holliston.high.app.list.EventsListFragment;
import info.holliston.high.app.list.LunchListFragment;
import info.holliston.high.app.list.NewsRecyclerFragment;
import info.holliston.high.app.list.SchedulesListFragment;

public class MainPagerFragment extends Fragment implements ViewPager.OnPageChangeListener {
    private int currentTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentTab = 0;
        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt("currentTab", 0);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt("currentTab", 0);
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.tab_pager,
                container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setPage(currentTab);
    }

    public void setPage(int i) {
        MainActivity.getsViewPager().setCurrentItem(i);
        currentTab = i;
        //onPageSelected(i);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                HomeFragment fragment0 = MainActivity.getsHomeFragment();
                fragment0.showFirstNews();

                break;
            case 1:
                SchedulesListFragment fragment1 = MainActivity.getsSchedFragment();
                fragment1.showFirst();
                break;
            case 2:
                NewsRecyclerFragment fragment2 = MainActivity.getsNewsFragment();
                fragment2.showFirst();
                break;
            case 3:
                DailyAnnListFragment fragment3 = MainActivity.getsDailyAnnFragment();
                fragment3.showFirst();
                break;
            case 4:
                EventsListFragment fragment4 = MainActivity.getsEventsFragment();
                fragment4.showFirst();
                break;
            case 5:
                LunchListFragment fragment5 = MainActivity.getsLunchFragment();
                fragment5.showFirst();
                break;
            case 6:
                SocialFragment fragment6 = MainActivity.getsSocialFragment();
                break;
            default:
                break;
        }
        currentTab = position;
        MainActivity.setsCurrentView(position);
        MainActivity.setsCurrentNewsItem(-1);
        MainActivity.refreshActionBar(getActivity());
        //this gets overridden only in NewsFragment
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentTab", currentTab);
    }
}

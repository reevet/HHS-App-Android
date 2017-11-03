package info.holliston.high.app.pager.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import info.holliston.high.app.MainActivity;

public class MainPagerAdapter extends FragmentPagerAdapter {
    private final MainActivity ma;


    public MainPagerAdapter(MainActivity ma, FragmentManager fm) {
        super(fm);
        this.ma = ma;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        switch (i) {
            case 0:
                fragment = ma.getsHomeFragment();
                break;
            case 1:
                fragment = ma.getsSchedFragment();
                break;
            case 2:
                fragment = ma.getsNewsFragment();
                break;
            case 3:
                fragment = ma.getsDailyAnnFragment();
                break;
            case 4:
                fragment = ma.getsEventsFragment();
                break;
            case 5:
                fragment = ma.getsLunchFragment();
                break;
            case 6:
                fragment = ma.getsSocialFragment();
                break;
            default:
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 7;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}

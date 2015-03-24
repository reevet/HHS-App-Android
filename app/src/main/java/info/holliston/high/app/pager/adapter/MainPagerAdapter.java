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
                fragment = MainActivity.getsHomeFragment();
                break;
            case 1:
                fragment = MainActivity.getsSchedFragment();
                break;
            case 2:
                fragment = MainActivity.getsNewsFragment();
                break;
            case 3:
                fragment = MainActivity.getsDailyAnnFragment();
                break;
            case 4:
                fragment = MainActivity.getsEventsFragment();
                break;
            case 5:
                fragment = MainActivity.getsLunchFragment();
                break;
            case 6:
                fragment = MainActivity.getsSocialFragment();
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

package info.holliston.high.app.pager.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import info.holliston.high.app.MainActivity;

public class TabPagerAdapter extends FragmentPagerAdapter {
MainActivity ma;


   public TabPagerAdapter(MainActivity ma, FragmentManager fm) {
       super(fm);
       this.ma = ma;
   }

    @Override
    public Fragment getItem(int i) {

        Fragment fragment = null;
        switch (i) {
            case 0:
                fragment = ma.mHomeFragment;
                break;
            case 1:
                fragment = ma.mSchedFragment;
                break;
            case 2:
                fragment = ma.mNewsFragment;
                break;
            case 3:
                fragment = ma.mDailyAnnFragment;
                break;
            case 4:
                fragment = ma.mEventsFragment;
                break;
            case 5:
                fragment = ma.mLunchFragment;
                break;
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

    @Override
    public int getItemPosition(Object object){
        return POSITION_NONE;
    }

}

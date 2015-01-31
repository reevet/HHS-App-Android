package info.holliston.high.app.pager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.holliston.high.app.R;
import info.holliston.high.app.adapter.TabPagerAdapter;

/**
 * Created by reevet on 1/26/2015.
 */
public class TabPager extends Fragment {
    TabPagerAdapter mTabPagerAdapter;
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTabPagerAdapter =
                new TabPagerAdapter(
                        getActivity().getSupportFragmentManager());
        mViewPager = (ViewPager) getActivity().findViewById(R.id.frame_pager);
        mViewPager.setAdapter(mTabPagerAdapter);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.tab_pager,
                container, false);

        return v;
    }

    public void setPage(int i) {
        mViewPager.setCurrentItem(i);
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }
}

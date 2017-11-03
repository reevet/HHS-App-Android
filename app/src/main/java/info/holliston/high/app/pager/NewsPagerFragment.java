package info.holliston.high.app.pager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.ArticleWarehouse;
import info.holliston.high.app.pager.adapter.NewsPagerAdapter;

public class NewsPagerFragment extends Fragment implements ViewPager.OnPageChangeListener {
    private NewsPagerAdapter mDetailPagerAdapter;
    private ViewPager mViewPager;
    private int position;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt("currentArticle", 0);
        }
        View v = inflater.inflate(R.layout.detail_pager,
                container, false);

        Bundle bundle = this.getArguments();
        position = bundle.getInt("position", 0);

        MainActivity activity = (MainActivity) getActivity();
        List<Article> articles = activity.getWarehouse().getAllArticles(ArticleWarehouse.StoreType.NEWS);

        mDetailPagerAdapter =
                new NewsPagerAdapter(
                        getActivity().getSupportFragmentManager());
        mViewPager = v.findViewById(R.id.detail_pager);
        mViewPager.setAdapter(mDetailPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mDetailPagerAdapter.setArticles(articles);
        mViewPager.setCurrentItem(position);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentArticle", position);
    }

    @Override
    public void onPageSelected(int position) {
        MainActivity activity = (MainActivity) getActivity();
        activity.setsCurrentNewsItem(position);
        activity.refreshActionBar(getActivity());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }
}

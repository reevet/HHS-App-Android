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
import info.holliston.high.app.pager.adapter.EventPagerAdapter;

public class EventPagerFragment extends Fragment {
    EventPagerAdapter mDetailPagerAdapter;
    ViewPager mViewPager;
    int position;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (savedInstanceState!=null){
            position = savedInstanceState.getInt("currentArticle", 0);
        }
        View v = inflater.inflate(R.layout.detail_pager,
                container, false);

        Bundle bundle = this.getArguments();
        position = bundle.getInt("position", 0);

        List<Article> articles;
        MainActivity ma = (MainActivity) getActivity();
        articles = ma.eventsSource.getAllArticles();

        mDetailPagerAdapter =
                new EventPagerAdapter(
                        getActivity().getSupportFragmentManager());
        mViewPager = (ViewPager) v.findViewById(R.id.detail_pager);
        mViewPager.setAdapter(mDetailPagerAdapter);
        mDetailPagerAdapter.setArticles(articles);
        mViewPager.setCurrentItem(position);

        return v;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentArticle", position);
    }
}

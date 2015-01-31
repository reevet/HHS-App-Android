package info.holliston.high.app.pager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import info.holliston.high.app.ArticleDataSource;
import info.holliston.high.app.ArticleDataSourceOptions;
import info.holliston.high.app.ArticleSQLiteHelper;
import info.holliston.high.app.R;
import info.holliston.high.app.adapter.LunchPagerAdapter;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.xmlparser.ArticleParser;

/**
 * Created by reevet on 1/23/2015.
 */
public class LunchPager extends Fragment {
    LunchPagerAdapter mDetailPagerAdapter;
    ViewPager mViewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.detail_pager,
                container, false);

        int i=0;
        Bundle bundle = this.getArguments();
        i = bundle.getInt("position");

        ArticleDataSource datasource;

        ArticleDataSourceOptions options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_LUNCH, getString(R.string.lunch_url),
                getResources().getStringArray(R.array.lunch_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS, ArticleDataSource.SortOrder.GET_FUTURE,
                "25");
        datasource = new ArticleDataSource(getActivity().getApplicationContext(),options);
        datasource.open();

        List<Article> articles;
        articles = datasource.getAllArticles();
        datasource.close();

        mDetailPagerAdapter =
                new LunchPagerAdapter(
                        getActivity().getSupportFragmentManager());
        mViewPager = (ViewPager) v.findViewById(R.id.detail_pager);
        mViewPager.setAdapter(mDetailPagerAdapter);
        mDetailPagerAdapter.setArticles(articles);
        mViewPager.setCurrentItem(i);

        return v;
    }
}

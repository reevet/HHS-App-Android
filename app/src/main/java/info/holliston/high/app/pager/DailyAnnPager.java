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
import info.holliston.high.app.adapter.DailyAnnPagerAdapter;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.xmlparser.ArticleParser;

/**
 * Created by reevet on 1/23/2015.
 */
public class DailyAnnPager extends Fragment {
    DailyAnnPagerAdapter mDetailPagerAdapter;
    ViewPager mViewPager;
    List<Article> articles;

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
                ArticleSQLiteHelper.TABLE_DAILYANN, getString(R.string.dailyann_url),
                getResources().getStringArray(R.array.dailyann_parser_names),
                ArticleParser.HtmlTags.CONVERT_LINE_BREAKS, ArticleDataSource.SortOrder.GET_PAST,
                "10");
        datasource = new ArticleDataSource(getActivity().getApplicationContext(), options );
        datasource.open();

        //articleStore = (ArticleStore) bundle.getSerializable("articleStore");
        //articles = articleStore.allArticles();
        articles = datasource.getAllArticles();
        datasource.close();

        mDetailPagerAdapter =
                new DailyAnnPagerAdapter(
                        getActivity().getSupportFragmentManager());
        mViewPager = (ViewPager) v.findViewById(R.id.detail_pager);
        mViewPager.setAdapter(mDetailPagerAdapter);
        mDetailPagerAdapter.setArticles(articles);
        mViewPager.setCurrentItem(i);

        return v;
    }
}

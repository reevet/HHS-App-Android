package info.holliston.high.app;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import info.holliston.high.app.adapter.DailyAnnArrayAdapter;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.xmlparser.ArticleParser;

public class DailyAnnListFragment extends ListFragment {

    public DailyAnnListFragment() {}
    private List<Article> articles;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dailyann_listview,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

        //articleStore = (ArticleStore) bundle.getSerializable("articleStore");
        //articles = articleStore.allArticles();

        DailyAnnArrayAdapter adapter = new DailyAnnArrayAdapter(getActivity(), articles);
        setListAdapter(adapter);

        if (getActivity().findViewById(R.id.frame_container) == null) {
            if (articles.size() > 0) {
                sendToDetailFragment(articles.get(0));
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Create new fragment and transaction
        sendToDetailFragment(articles.get(position));

    }

    private void sendToDetailFragment(Article sendArticle) {

        Fragment newFragment = new DailyAnnDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("detail_article", sendArticle);
        newFragment.setArguments(bundle);
        if (getActivity().findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_detail_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

}
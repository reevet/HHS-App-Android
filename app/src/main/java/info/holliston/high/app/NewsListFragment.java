package info.holliston.high.app;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import info.holliston.high.app.adapter.NewsArrayAdapter;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.xmlparser.ArticleParser;

public class NewsListFragment extends ListFragment {

    public NewsListFragment() {}
    private List<Article> articles;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.news_listview,
                container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArticleDataSource datasource;

        ArticleDataSourceOptions options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_NEWS, getString(R.string.news_url),
                getResources().getStringArray(R.array.news_parser_names),
                ArticleParser.HtmlTags.KEEP_HTML_TAGS, ArticleDataSource.SortOrder.GET_PAST,
                "25");
        datasource = new ArticleDataSource(getActivity().getApplicationContext(),options);
        datasource.open();

        //articleStore = (ArticleStore) bundle.getSerializable("articleStore");
        //articles = articleStore.allArticles();
        articles = datasource.getAllArticles();
        //articleStore = (ArticleStore) bundle.getSerializable("articleStore");
        //articles = articleStore.allArticles();
        datasource.close();

        NewsArrayAdapter adapter = new NewsArrayAdapter(getActivity(), articles);
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

        Fragment newFragment = new NewsDetailFragment();
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
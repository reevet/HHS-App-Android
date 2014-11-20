package info.holliston.high.app;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import info.holliston.high.app.adapter.NewsCardAdapter;
import info.holliston.high.app.adapter.RecyclerItemClickListener;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.xmlparser.ArticleParser;

public class NewsRecyclerFragment extends Fragment {

    public NewsRecyclerFragment() {}
    private List<Article> articles;
    NewsCardAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.news_recyclerview,
                container, false);
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

        adapter = new NewsCardAdapter(getActivity(), articles);
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.cardlist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),1));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        sendToDetailFragment(articles.get(position));
                    }
                })
        );

        if (getActivity().findViewById(R.id.frame_container) == null) {
            if (articles.size() > 0) {
                sendToDetailFragment(articles.get(0));
            }
        }

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
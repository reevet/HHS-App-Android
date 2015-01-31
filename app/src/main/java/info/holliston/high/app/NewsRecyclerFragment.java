package info.holliston.high.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.List;

import info.holliston.high.app.adapter.NewsCardAdapter;
import info.holliston.high.app.adapter.RecyclerItemClickListener;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.pager.NewsPager;
import info.holliston.high.app.xmlparser.ArticleParser;

public class NewsRecyclerFragment extends Fragment {

    public NewsRecyclerFragment() {}
    private List<Article> articles;
    NewsCardAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.news_recyclerview,
                container, false);
        final SwipeRefreshLayout swipeLayout=(SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.cardlist);
        recyclerView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = recyclerView.getScrollY();
                if(scrollY == 0) swipeLayout.setEnabled(true);
                else swipeLayout.setEnabled(false);

            }
        });
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

        adapter = new NewsCardAdapter(getActivity(), articles);
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.cardlist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),1));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        sendToDetailFragment(position);
                    }
                })
        );

        if (getActivity().findViewById(R.id.frame_pager) == null) {
            if (articles.size() > 0) {
                sendToDetailFragment(0);
            }
        }

    }

    private void sendToDetailFragment(int i) {

        NewsPager newFragment = new NewsPager();
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        newFragment.setArguments(bundle);
        if (getActivity().findViewById(R.id.news_frame) != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.news_frame, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_detail_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

}
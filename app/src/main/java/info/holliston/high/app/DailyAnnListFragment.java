package info.holliston.high.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import info.holliston.high.app.adapter.DailyAnnArrayAdapter;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.pager.DailyAnnPager;
import info.holliston.high.app.xmlparser.ArticleParser;

public class DailyAnnListFragment extends Fragment {

    public DailyAnnListFragment() {}
    private List<Article> articles;
    private ArticleDataSource datasource;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dailyann_listview,
                container, false);
        final SwipeRefreshLayout swipeLayout=(SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        final ListView listView = (ListView) v.findViewById(R.id.dailyann_list);
        listView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = listView.getScrollY();
                if(scrollY == 0) swipeLayout.setEnabled(true);
                else swipeLayout.setEnabled(false);

            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
        ListView lv = (ListView) getActivity().findViewById(R.id.dailyann_list);
        lv.setAdapter(adapter);

        if (getActivity().findViewById(R.id.frame_pager) == null) {
            if (articles.size() > 0) {
                sendToDetailFragment(0);
            }
        }

        // Listview on child click listener
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    sendToDetailFragment(position);
                    return;
                }
            }

        );
    }

    private void sendToDetailFragment(int i) {

        DailyAnnPager newFragment = new DailyAnnPager();
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        newFragment.setArguments(bundle);
        if (getActivity().findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, newFragment);
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
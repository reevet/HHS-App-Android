package info.holliston.high.app.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.list.adapter.NewsCardAdapter;
import info.holliston.high.app.list.adapter.RecyclerItemClickListener;
import info.holliston.high.app.pager.NewsPagerFragment;

public class NewsRecyclerFragment extends Fragment {

    NewsCardAdapter adapter;
    private List<Article> articles;
    private int currentArticle;

    public NewsRecyclerFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentArticle = -1;
        if (savedInstanceState != null) {
            currentArticle = savedInstanceState.getInt("currentArticle", 0);
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.news_recyclerview,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateUI();
    }

    public void updateUI() {
        MainActivity ma = (MainActivity) getActivity();
        articles = ma.newsSource.getAllArticles();

        adapter = new NewsCardAdapter(getActivity(), articles);
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.cardlist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        sendToDetailFragment(position);
                    }
                })
        );

        if ((currentArticle >= 0) || ma.newNewsAvailable) {
            sendToDetailFragment(currentArticle);
            ma.newNewsAvailable = false;
        }
    }

    private void sendToDetailFragment(int i) {

        NewsPagerFragment newFragment = new NewsPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        newFragment.setArguments(bundle);
        if (getActivity().findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, newFragment, "newsPager");
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_detail_container, newFragment, "newsPager");
            transaction.addToBackStack(null);
            transaction.commit();
        }
        currentArticle = i;
    }

    public void showFirst() {
        if (getActivity().findViewById(R.id.frame_detail_container) != null) {
            if (articles.size() > 0) {
                sendToDetailFragment(0);
            } else {
                Fragment blankFragment = new Fragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_detail_container, blankFragment);
                transaction.commit();

            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentArticle", currentArticle);
    }
}
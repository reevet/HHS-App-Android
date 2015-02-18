package info.holliston.high.app.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.list.adapter.DailyAnnArrayAdapter;
import info.holliston.high.app.pager.DailyAnnPagerFragment;
import info.holliston.high.app.pager.adapter.TabPagerAdapter;

public class DailyAnnListFragment extends Fragment {

    public DailyAnnListFragment() {}
    private TabPagerAdapter parentPagerAdapter;
    private List<Article> articles;
    private int currentArticle;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentArticle = -1;
        if (savedInstanceState != null) {
            currentArticle = savedInstanceState.getInt("currentArticle", 0);
        }

        return inflater.inflate(R.layout.dailyann_listview,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

       updateUI();
    }

    public void updateUI() {
        MainActivity ma = (MainActivity) getActivity();
        articles = ma.dailyannSource.getAllArticles();

        DailyAnnArrayAdapter adapter = new DailyAnnArrayAdapter(getActivity(), articles);
        ListView lv = (ListView) getActivity().findViewById(R.id.dailyann_list);
        lv.setAdapter(adapter);

        // Listview on child click listener
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {

                                      @Override
                                      public void onItemClick(AdapterView<?> parent, View view,
                                                              int position, long id) {
                                          sendToDetailFragment(position);
                                      }
                                  }
        );

        if (currentArticle >=0) {
            sendToDetailFragment(currentArticle);
        }
    }

    private void sendToDetailFragment(int i) {

        DailyAnnPagerFragment newFragment = new DailyAnnPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        newFragment.setArguments(bundle);
        if (getActivity().findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, newFragment, "dailyAnnPager");
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_detail_container, newFragment, "dailyAnnPager");
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
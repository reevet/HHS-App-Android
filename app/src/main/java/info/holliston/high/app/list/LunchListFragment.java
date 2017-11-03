package info.holliston.high.app.list;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.ArticleWarehouse;
import info.holliston.high.app.list.adapter.LunchArrayAdapter;
import info.holliston.high.app.pager.LunchPagerFragment;

public class LunchListFragment extends Fragment {

    private final List<String> headers = new ArrayList<>();
    private final HashMap<String, List<Article>> lunches = new HashMap<>();
    private View v;
    private ExpandableListView lv;
    private LunchArrayAdapter adapter;
    private int currentArticle;

    public LunchListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentArticle = -1;
        if (savedInstanceState != null) {
            currentArticle = savedInstanceState.getInt("currentArticle", 0);
        }
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.lunch_exlistview,
                container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateUI();

        this.lv = v.findViewById(R.id.lunch_exlistview);

        adapter = new LunchArrayAdapter(getActivity(), this.headers, this.lunches);
        this.lv.setAdapter(adapter);

        ExpandableListView.OnGroupClickListener gcl = new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                parent.expandGroup(groupPosition);
                return true;
            }
        };

        this.lv.setOnGroupClickListener(gcl);

        // Listview on child click listener
        this.lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                                            @Override
                                            public boolean onChildClick(ExpandableListView parent, View v,
                                                                        int groupPosition, int childPosition, long id) {
                                                sendToDetailFragment(childPosition);
                                                return false;
                                            }
                                        }

        );
        if (currentArticle >= 0) {
            sendToDetailFragment(currentArticle);
        }
    }

    public void updateUI() {
        List<Article> articles;
        MainActivity activity = (MainActivity) getActivity();
        articles = activity.getWarehouse().getAllArticles(ArticleWarehouse.StoreType.LUNCH);

        if (headers.size() == 0) {
            int weekOfYear = -1;
            String currentHeader = "";
            List<Article> eventsInDay = new ArrayList<>();

            //check if now is after school - if so, skip today's lunch
            if (articles.size() >= 2) {
                Date todayDate = new Date();
                Calendar todayCal = Calendar.getInstance();
                todayCal.setTime(todayDate);
                int todayMonth = todayCal.get(Calendar.MONTH);
                int todayDay = todayCal.get(Calendar.DATE);
                int todayHour = todayCal.get(Calendar.HOUR_OF_DAY);

                if (todayHour >= 14) {
                    Date firstDate = articles.get(0).date;
                    Calendar firstCal = Calendar.getInstance();
                    firstCal.setTime(firstDate);
                    int firstMonth = firstCal.get(Calendar.MONTH);
                    int firstDay = firstCal.get(Calendar.DATE);

                    if ((todayMonth == firstMonth) && (todayDay == firstDay)) {
                        articles.remove(0);
                    }
                }
            }

            for (Article article : articles) {
                Date date = article.date;
                if (date == null) {
                    return;
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int thisWeek = cal.get(Calendar.WEEK_OF_YEAR);

                if (thisWeek != weekOfYear) {
                    weekOfYear = thisWeek;
                    if (!(currentHeader.equals(""))) {
                        this.lunches.put(currentHeader, eventsInDay);
                        eventsInDay = new ArrayList<>();
                    }
                    SimpleDateFormat hFormat = new SimpleDateFormat("EEE, MMM d");
                    String headerString = hFormat.format(date);
                    this.headers.add(headerString);
                    currentHeader = headerString;

                }
                eventsInDay.add(article);
            }
            if (eventsInDay.size() > 0) {
                this.lunches.put(currentHeader, eventsInDay);
            }
        }


        if (this.adapter != null) {
            adapter.notifyDataSetChanged();
            for (int i = 0; i < headers.size(); i++) {
                lv.expandGroup(i);
            }
        }
    }

    private void sendToDetailFragment(int i) {
        LunchPagerFragment newFragment = new LunchPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        newFragment.setArguments(bundle);

        if (getActivity().findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, newFragment, "lunchPager");
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_detail_container, newFragment, "lunchPager");
            transaction.addToBackStack(null);
            transaction.commit();
        }
        currentArticle = i;
    }


    @Override
    public void onStart() {
        super.onStart();
        for (int i = 0; i < this.headers.size(); i++) {
            this.lv.expandGroup(i);
        }
    }

    public void showFirst() {
        if (getActivity().findViewById(R.id.frame_detail_container) != null) {
            if (lunches.size() > 0) {
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

    public void setCurrentArticle(int currentArticle) {
        this.currentArticle = currentArticle;
    }
}
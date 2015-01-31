package info.holliston.high.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ExpandableListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import info.holliston.high.app.adapter.LunchArrayAdapter;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.pager.LunchPager;
import info.holliston.high.app.xmlparser.ArticleParser;

public class LunchListFragment extends Fragment {

    public LunchListFragment() {}

    private ArticleDataSource datasource;
    List<String> headers = new ArrayList<String>();
    HashMap<String, List<Article>> lunches = new HashMap<String, List<Article>>();

    View v;
    ExpandableListView lv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.lunch_exlistview,
                container, false);
        final SwipeRefreshLayout swipeLayout=(SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        final ExpandableListView listView = (ExpandableListView) v.findViewById(R.id.lunch_exlistview);
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
                ArticleSQLiteHelper.TABLE_LUNCH, getString(R.string.lunch_url),
                getResources().getStringArray(R.array.lunch_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS, ArticleDataSource.SortOrder.GET_FUTURE,
                "25");
        datasource = new ArticleDataSource(getActivity().getApplicationContext(),options);
        datasource.open();

        List<Article> articles;
        articles = datasource.getAllArticles();
        datasource.close();

        if(headers.size() == 0) {
            int weekOfYear = -1;
            String currentHeader = "";
            List<Article> eventsInDay = new ArrayList<Article>();

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
                        eventsInDay = new ArrayList<Article>();
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
        this.lv = (ExpandableListView) v.findViewById(R.id.lunch_exlistview);

        LunchArrayAdapter adapter = new LunchArrayAdapter(getActivity(), this.headers, this.lunches);
        this.lv.setAdapter(adapter);

        if (getActivity().findViewById(R.id.frame_pager) == null) {
            if (lunches.size() > 0) {
                sendToDetailFragment(0);
            }
        }

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
                    // Create new fragment and transaction
                    String header = headers.get(groupPosition);
                    List<Article> groupArticles = lunches.get(header);
                    Article sendArticle = groupArticles.get(childPosition);

                    sendToDetailFragment(childPosition);
                    return false;
                }
            }

        );
    }

    private void sendToDetailFragment(int i) {

        LunchPager newFragment = new LunchPager();
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        newFragment.setArguments(bundle);
        if (getActivity().findViewById(R.id.lunch_frame) != null) {
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


    @Override
    public void onStart() {
        super.onStart();
        for (int i=0; i< this.headers.size(); i++) {
            this.lv.expandGroup(i);
        }
    }

    @Override
    public void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        datasource.close();
        super.onPause();
    }
}
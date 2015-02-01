package info.holliston.high.app;

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

import info.holliston.high.app.adapter.SchedulesArrayAdapter;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.pager.SchedulePager;
import info.holliston.high.app.xmlparser.ArticleParser;

public class SchedulesListFragment extends Fragment {

    public SchedulesListFragment() {}

    private ArticleDataSource datasource;
    List<String> headers = new ArrayList<String>();
    HashMap<String, List<Article>> schedules = new HashMap<String, List<Article>>();

    View v;
    ExpandableListView lv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.schedules_exlistview,
                container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArticleDataSourceOptions options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_SCHEDULES, getString(R.string.schedules_url),
                getResources().getStringArray(R.array.schedules_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS, ArticleDataSource.SortOrder.GET_FUTURE,
                "25");
        datasource = new ArticleDataSource(getActivity().getApplicationContext(),options);
        datasource.open();

        List<Article> articles;
        articles = datasource.getAllArticles();
        datasource.close();

        if (headers.size() == 0) {
            int weekOfYear = -1;
            String currentHeader = "";
            List<Article> eventsInDay = new ArrayList<Article>();

            //check if now is after school - if so, skip today's schedule
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
                        this.schedules.put(currentHeader, eventsInDay);
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
                this.schedules.put(currentHeader, eventsInDay);
            }
        }
            this.lv = (ExpandableListView) v.findViewById(R.id.schedules_exlistview);

            SchedulesArrayAdapter adapter = new SchedulesArrayAdapter(getActivity(), this.headers, this.schedules);
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
                                                    // Create new fragment and transaction
                                                    String header = headers.get(groupPosition);
                                                    List<Article> groupArticles = schedules.get(header);
                                                    Article sendArticle = groupArticles.get(childPosition);

                                                    sendToDetailFragment(childPosition);
                                                    return false;
                                                }
                                            }

            );


        //SchedulesArrayAdapter adapter = new SchedulesArrayAdapter(getActivity(), articles);
        //setListAdapter(adapter);

    }

    private void sendToDetailFragment(int i){
        SchedulePager newFragment = new SchedulePager();
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

    public void showFirst() {
        if (getActivity().findViewById(R.id.frame_detail_container) != null) {
            if (schedules.size() > 0) {
                sendToDetailFragment(0);
            }
        }
    }
}
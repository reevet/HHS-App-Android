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
import info.holliston.high.app.list.adapter.EventsArrayAdapter;
import info.holliston.high.app.pager.EventPagerFragment;

public class EventsListFragment extends Fragment {

    private final List<String> headers = new ArrayList<>();
    private final HashMap<String, List<Article>> events = new HashMap<>();
    private View v;
    private ExpandableListView lv;
    private EventsArrayAdapter adapter;
    private int currentArticle;

    public EventsListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentArticle = -1;
        if (savedInstanceState != null) {
            currentArticle = savedInstanceState.getInt("currentArticle", 0);
        }
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.events_exlistview,
                container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateUI();

        this.lv = (ExpandableListView) v.findViewById(R.id.events_exlistview);

        this.adapter = new EventsArrayAdapter(getActivity(), this.headers, this.events);
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
                int counter = 0;
                for (int j = 0; j <= groupPosition; j++) {
                    String headerName = headers.get(j);
                    List<Article> a = events.get(headerName);
                    for (int i = 0; i < a.size(); i++) {
                        if ((j == groupPosition) && (i == childPosition)) {
                            String details = a.get(i).details;
                            if (!details.equals("")) {
                                sendToDetailFragment(counter);
                            }
                            break;
                        } else {
                            counter++;
                        }
                    }
                }
                return false;
            }
        });
        if (currentArticle >= 0) {
            sendToDetailFragment(currentArticle);
        }
    }

    public void updateUI() {
        List<Article> articles;
        articles = MainActivity.getsEventsSource().getAllArticles();
        headers.clear();
        events.clear();


        int dayOfYear = -1;
        String currentHeader = "";
        List<Article> eventsInDay = new ArrayList<>();


        for (Article article : articles) {
            Date date = article.date;
            if (date == null) {
                return;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int thisDay = cal.get(Calendar.DAY_OF_YEAR);

            if (thisDay != dayOfYear) {
                dayOfYear = thisDay;
                if (!(currentHeader.equals(""))) {
                    this.events.put(currentHeader, eventsInDay);
                    eventsInDay = new ArrayList<>();
                }
                SimpleDateFormat hFormat = new SimpleDateFormat("EEEE, MMMM d");
                String headerString = hFormat.format(date);
                this.headers.add(headerString);
                currentHeader = headerString;

            }
            eventsInDay.add(article);
        }
        if (eventsInDay.size() > 0) {
            this.events.put(currentHeader, eventsInDay);
        }

        if (this.adapter != null) {
            adapter.notifyDataSetChanged();
            for (int i = 0; i < headers.size(); i++) {
                lv.expandGroup(i);
            }
        }


    }

    private void sendToDetailFragment(int i) {

        EventPagerFragment newFragment = new EventPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        newFragment.setArguments(bundle);
        if (getActivity().findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, newFragment, "eventsPager");
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_detail_container, newFragment, "eventsPager");
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
            if (events.size() > 0) {
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
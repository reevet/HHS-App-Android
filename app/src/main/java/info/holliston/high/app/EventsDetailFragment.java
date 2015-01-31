package info.holliston.high.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import info.holliston.high.app.model.Article;

public class EventsDetailFragment extends Fragment {

    public EventsDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        Article article = (Article) bundle.getSerializable("detail_article");

        View rootView = inflater.inflate(R.layout.events_detail, container, false);
        final SwipeRefreshLayout swipeLayout=(SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        swipeLayout.setEnabled(false);

        TextView titleTextView = (TextView) rootView.findViewById(R.id.detail_title);
        titleTextView.setText(article.title);

        SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d");
        SimpleDateFormat tf = new SimpleDateFormat("h:mm a");
        String dateString = df.format(article.date);
        String timeString = tf.format(article.date);

        if (!(timeString.equals("12:00 AM"))) {
         dateString += ", " + timeString;
        }
        TextView dateTextView = (TextView) rootView.findViewById(R.id.detail_date);
        dateTextView.setText(dateString);

        TextView detailTextView = (TextView) rootView.findViewById(R.id.detail_details);
        String detailsString = article.details;
        if (detailsString.equals("")) {
            detailTextView.setText("No additional details posted");
        } else {
            detailTextView.setText(detailsString);
        }

        return rootView;
    }

}
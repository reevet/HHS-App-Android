package info.holliston.high.app.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

public class ScheduleDetailFragment extends Fragment {

    private Article article;

    public ScheduleDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        article = (Article) bundle.getSerializable("detail_article");

        View rootView = inflater.inflate(R.layout.schedule_detail, container, false);

        TextView titleTextView = (TextView) rootView.findViewById(R.id.detail_title);
        titleTextView.setText(article.title);

        SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d");
        String dateString = df.format(article.date);
        TextView dateTextView = (TextView) rootView.findViewById(R.id.detail_date);
        dateTextView.setText(dateString);

        TextView detailTextView = (TextView) rootView.findViewById(R.id.detail_details);
        String details = article.details;
        details = details.replace("\t", " \t ");
        detailTextView.setText(details);

        char initial = article.title.charAt(0);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_icon);

        switch (initial) {
            case 'A':
                imageView.setImageResource(R.drawable.a_lg);
                break;
            case 'B':
                imageView.setImageResource(R.drawable.b_lg);
                break;
            case 'C':
                imageView.setImageResource(R.drawable.c_lg);
                break;
            case 'D':
                imageView.setImageResource(R.drawable.d_lg);
                break;
            default:
                imageView.setImageResource(R.drawable.star_lg);
                break;
        }


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("detail_article", article);

    }

}
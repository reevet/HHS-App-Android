package info.holliston.high.app.detail;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

public class DailyAnnDetailFragment extends Fragment {
    Article article;
    public DailyAnnDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        article = (Article) bundle.getSerializable("detail_article");

        View rootView = inflater.inflate(R.layout.dailyann_detail, container, false);

        String titleString = article.title;

        Date date;
        String formattedDateString;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            date = sdf.parse(titleString);
            SimpleDateFormat ndf = new SimpleDateFormat("EEEE, MMMM d");
            formattedDateString = ndf.format(date);
        } catch (Exception e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d");
                date = sdf.parse(titleString);
                SimpleDateFormat ndf = new SimpleDateFormat("EEEE, MMMM d");
                formattedDateString = ndf.format(date);
            } catch (Exception ex) {
                formattedDateString = titleString;
            }
        }

        TextView titleTextView = (TextView) rootView.findViewById(R.id.detail_date);
        titleTextView.setText(formattedDateString);

        String detailString = article.details;
        detailString = detailString.replace("</div>","\n" );
        //detailString = detailString.replace("\n\n","\n" );
        TextView detailTextView = (TextView) rootView.findViewById(R.id.detail_details);
        detailTextView.setText(detailString);

        return rootView;
    }
}
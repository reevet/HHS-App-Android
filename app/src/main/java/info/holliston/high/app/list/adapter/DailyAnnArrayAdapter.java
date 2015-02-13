package info.holliston.high.app.list.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

public class DailyAnnArrayAdapter extends ArrayAdapter<Article> {
private final Context context;
private final List<Article> articleList;

    public DailyAnnArrayAdapter(Context context, List<Article> list) {
        super(context, R.layout.dailyann_row, list);
        this.context = context;
        this.articleList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dailyann_row, parent, false);
        }

        String titleString = articleList.get(position).title;


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

        TextView titleTextView = (TextView) convertView.findViewById(R.id.row_title);

        titleTextView.setText(formattedDateString);



        return convertView;
    }
}


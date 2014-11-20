package info.holliston.high.app.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import info.holliston.high.app.R;
import info.holliston.high.app.model.Article;

public class EventsArrayAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Article>> _listDataChild;

    public EventsArrayAdapter(Context context, List<String> listDataHeader,
                              HashMap<String, List<Article>> listChildData) {
        //super(context, R.layout.events_row, list);
        this.context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Article article = (Article) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.events_row, parent, false);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.row_title);
        TextView dateListChild = (TextView) convertView
                .findViewById(R.id.row_time);

        ImageView moreIcon = (ImageView) convertView
                .findViewById(R.id.row_disc_icon);



        SimpleDateFormat df = new SimpleDateFormat("h:mm a");
        String dateString = df.format(article.date);
        if (dateString.equals("12:00 AM")) {
            dateString = "All Day";
        }
        dateListChild.setText(dateString);

        String titleString = article.title;
        txtListChild.setText(titleString);

        if (article.details.equals("")) {
            moreIcon.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.events_list_header, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.header_title);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}



package info.holliston.high.app.list.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

public class LunchArrayAdapter extends BaseExpandableListAdapter { // ArrayAdapter<Article> {
    private final Context context;
    private final List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private final HashMap<String, List<Article>> _listDataChild;
    //private final ArrayList<Article> articleList;

    public LunchArrayAdapter(Context context, List<String> listDataHeader,
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
            convertView = inflater.inflate(R.layout.lunch_row, parent, false);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.row_title);
        TextView dateListChild = (TextView) convertView
                .findViewById(R.id.row_date);

        SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d");
        String dateString = df.format(article.date);
        String titleString = article.title;

        dateListChild.setText(dateString);
        txtListChild.setText(titleString);

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
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.lunch_list_header, parent, false);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.header_title);
        if (groupPosition == 0) {
            lblListHeader.setText("This week");
        } else if (groupPosition == 1) {
            lblListHeader.setText("Next week");
        } else if (groupPosition == 2) {
            lblListHeader.setText("Later");
        } else {
            lblListHeader.setText("");
        }

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

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.schedules_row, parent, false);
        TextView titleTextView = (TextView) rowView.findViewById(R.id.row_title);
        titleTextView.setText(articleList.get(position).title);

        SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d");
        String dateString = df.format(articleList.get(position).date);
        TextView dateTextView = (TextView) rowView.findViewById(R.id.row_date);
        dateTextView.setText(dateString);

        char initial = articleList.get(position).title.charAt(0);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.row_icon);

        switch (initial) {
            case 'A' :
                imageView.setImageResource(R.drawable.a_50);
                break;
            case 'B' :
                imageView.setImageResource(R.drawable.b_50);
                break;
            case 'C' :
                imageView.setImageResource(R.drawable.c_50);
                break;
            case 'D' :
                imageView.setImageResource(R.drawable.d_50);
                break;
            default :
                imageView.setImageResource(R.drawable.star_50);
                break;
        }

        return rowView;
    }*/
}


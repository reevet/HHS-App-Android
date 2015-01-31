package info.holliston.high.app;

import android.support.v4.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import info.holliston.high.app.model.Article;

public class NewsDetailFragment extends Fragment {

    public NewsDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        Article article = (Article) bundle.getSerializable("detail_article");

        View rootView = inflater.inflate(R.layout.news_detail, container, false);
        final SwipeRefreshLayout swipeLayout=(SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        swipeLayout.setEnabled(false);

        TextView titleTextView = (TextView) rootView.findViewById(R.id.detail_title);
        titleTextView.setText(article.title);

        SimpleDateFormat df = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        String dateString ="";
        try {
            dateString = df.format(article.date);
        } catch (Exception e)
        {
            //error
        }
        TextView dateTextView = (TextView) rootView.findViewById(R.id.detail_date);
        dateTextView.setText(dateString);

        String details = article.details;
        details = details.replace("<img","<img style='max-width: 100%;' ");
        if ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) &&
        (getResources().getConfiguration().screenWidthDp <=480)) {
            details = details.replace("float: left", "");
            details = details.replace("float:left", "");
            details = details.replace("float: right", "");
            details = details.replace("float:right", "");
            details = details.replace("display: inline", "display: block");
            details = details.replace("display:inline", "display:block");
        }
        details = details.replace("\t", ": ");
        details = details.replace("</br>", "");
        details = details.replace("<br>", "<br />");
        details = details.replace("\n  ", "<br />");
        details = details.replace("\n", "<br />");
        details = details.replace("<br /><br />", "<br />");
        details = details.replace("<br />\n<br />", "<br />");
        details = details.replace("</p><br />", "</p>");
        details = details.replace(" – ", "&#45;");
        details = details.replace("\u00A0", " ");
        details = details.replace("\u2019", "&#39");
        details = details.replaceFirst("<hr.+>","");

        WebView detailWebView = (WebView) rootView.findViewById(R.id.detail_webview);
        detailWebView.loadData(details, "text/html", null);

        return rootView;
    }

}
package info.holliston.high.app;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import info.holliston.high.app.adapter.ImageAsyncLoader;
import info.holliston.high.app.model.Article;
import info.holliston.high.app.xmlparser.ArticleParser;

public class HomeFragment extends Fragment {

    private ArticleDataSource newsSource;
    private ArticleDataSource schedulesSource;
    private ArticleDataSource dailyAnnSource;


    List<String> eventHeaders = new ArrayList<String>();
    HashMap<String, List<Article>> events = new HashMap<String, List<Article>>();

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.home_fragment,
                container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArticleDataSource eventsSource;

        ArticleDataSourceOptions options;
        List<Article> tempArticles;
        Article scheduleArticle;
        Article newsArticle;
        Article dailyAnnArticle;
        List<Article> eventsArticles;

        /*Get the most recent news item */
        options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_NEWS, getString(R.string.news_url),
                getResources().getStringArray(R.array.news_parser_names),
                ArticleParser.HtmlTags.KEEP_HTML_TAGS, ArticleDataSource.SortOrder.GET_PAST,
                "1");
        newsSource = new ArticleDataSource(getActivity().getApplicationContext(),options);
        newsSource.open();

        tempArticles = newsSource.getAllArticles();
        newsSource.close();

        if (tempArticles.size() >0) {
            newsArticle = tempArticles.get(0);
            assignNews(newsArticle);
        }

        /*Get the most recent schedule */
        options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_SCHEDULES, getString(R.string.schedules_url),
                getResources().getStringArray(R.array.schedules_parser_names),
                ArticleParser.HtmlTags.IGNORE_HTML_TAGS, ArticleDataSource.SortOrder.GET_FUTURE,
                "2");
        schedulesSource = new ArticleDataSource(getActivity().getApplicationContext(),options);
        schedulesSource.open();

        tempArticles = schedulesSource.getAllArticles();
        schedulesSource.close();

        if (tempArticles.size() >0) {
            scheduleArticle = tempArticles.get(0);

            if(tempArticles.size() >=2) {
                Date todayDate = new Date();
                Calendar todayCal = Calendar.getInstance();
                todayCal.setTime(todayDate);
                int todayMonth = todayCal.get(Calendar.MONTH);
                int todayDay = todayCal.get(Calendar.DATE);
                int todayHour = todayCal.get(Calendar.HOUR_OF_DAY);

                if (todayHour >=14) {
                    Date firstDate = tempArticles.get(0).date;
                    Calendar firstCal = Calendar.getInstance();
                    firstCal.setTime(firstDate);
                    int firstMonth = firstCal.get(Calendar.MONTH);
                    int firstDay = firstCal.get(Calendar.DATE);

                    if ((todayMonth == firstMonth) && (todayDay == firstDay)) {
                        scheduleArticle = tempArticles.get(1);
                    }
                }
            }
            assignSchedule(scheduleArticle);
        }


        /*Get the most recent daily announcements */
        options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_DAILYANN, getString(R.string.dailyann_url),
                getResources().getStringArray(R.array.dailyann_parser_names),
                ArticleParser.HtmlTags.CONVERT_LINE_BREAKS, ArticleDataSource.SortOrder.GET_PAST,
                "1");
        dailyAnnSource = new ArticleDataSource(getActivity().getApplicationContext(),options);
        dailyAnnSource.open();

        tempArticles = dailyAnnSource.getAllArticles();
        dailyAnnSource.close();

        if (tempArticles.size() >0) {
            dailyAnnArticle = tempArticles.get(0);
            assignDailyAnn(dailyAnnArticle);
        }

        /*Get the most recent daily announcements */
        options = new ArticleDataSourceOptions(
                ArticleSQLiteHelper.TABLE_EVENTS, getString(R.string.events_url),
                getResources().getStringArray(R.array.events_parser_names),
                ArticleParser.HtmlTags.CONVERT_LINE_BREAKS, ArticleDataSource.SortOrder.GET_FUTURE,
                "12");
        eventsSource = new ArticleDataSource(getActivity().getApplicationContext(),options);
        eventsSource.open();

        eventsArticles = eventsSource.getAllArticles();
        eventsSource.close();

        TableLayout eventsTable = (TableLayout) v.findViewById(R.id.events_box);
        if ((eventsTable.getChildCount() == 0) && (eventsArticles.size() >0)){
            assignEvents(eventsArticles);
        }

    }

    private void assignNews(Article article) {
        ImageAsyncLoader.ViewHolder holder;
        ImageView imageView = (ImageView) v.findViewById(R.id.news_image);

        holder = new ImageAsyncLoader.ViewHolder();
        v.setTag(holder);

        holder.position = 0;

        holder.thumbnail = imageView;

        holder.thumbnail.setVisibility(View.INVISIBLE);

        holder.loading = (ProgressBar) v.findViewById(R.id.news_pbar);
        holder.loading.setVisibility(View.VISIBLE);

        TextView titlesTextView = (TextView) v.findViewById(R.id.news_title);
        titlesTextView.setText(article.title);

        String imgSrc = article.imgSrc;
        if ((imgSrc != null) && (imgSrc.length() > 0)) {
            //imageDownloader.setMode(ImageDownloader.Mode.NO_DOWNLOADED_DRAWABLE);
            //imageDownloader.download(articleList.get(position).imgSrc, (ImageView) rowView.findViewById(R.id.row_icon));

            int newHeight = (int) v.getResources().getDimension(R.dimen.news_home_height);
            int newWidth = 600;

            UUID key = article.key;
            ImageAsyncLoader ial = new ImageAsyncLoader(0, holder,
                    newWidth, newHeight,
                    ImageAsyncLoader.FitMode.FIT, ImageAsyncLoader.SourceMode.ALLOW_BOTH,
                    key, getActivity().getApplicationContext());
            //DownloadedDrawable downloadedDrawable = new DownloadedDrawable(ial);
            ial.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imgSrc);

        }

        View box = v.findViewById(R.id.news_box);
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newsSource.open();
                Article article = newsSource.getArticle(0);
                newsSource.close();
                Fragment newFragment = new NewsDetailFragment();
                sendToDetailFragment(article, newFragment);
            }
        };
        box.setOnClickListener(cl);
    }

    private void assignSchedule(Article article) {

        TextView txtListChild = (TextView) v.findViewById(R.id.sched_title);
        TextView dateListChild = (TextView) v.findViewById(R.id.sched_date);

        SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d");
        String dateString = df.format(article.date);
        String titleString = article.title;

        dateListChild.setText(dateString);
        txtListChild.setText(titleString);

        char initial = article.title.charAt(0);
        ImageView imageView = (ImageView) v.findViewById(R.id.sched_icon);

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

        View box = v.findViewById(R.id.schedule_box);
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schedulesSource.open();
                Article article = schedulesSource.getArticle(0);
                schedulesSource.close();
                Fragment newFragment = new ScheduleDetailFragment();
                sendToDetailFragment(article, newFragment);
            }
        };
        box.setOnClickListener(cl);
    }

    private void assignDailyAnn(Article article) {
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

        TextView titleTextView = (TextView) v.findViewById(R.id.dailyann_date);
        titleTextView.setText(formattedDateString);

        View box =  v.findViewById(R.id.dailyann_box);
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dailyAnnSource.open();
                Article article = dailyAnnSource.getArticle(0);
                dailyAnnSource.close();
                Fragment newFragment = new DailyAnnDetailFragment();
                sendToDetailFragment(article, newFragment);
            }
        };
        box.setOnClickListener(cl);
    }

    private void assignEvents(List<Article> articles) {
        int dayOfYear = -1;
        String currentHeader = "";
        List<Article> eventsInDay = new ArrayList<Article>();
        int numOfDays = 0;

        for (Article article : articles) {
            Date date = article.date;
            if (date == null) {
                return;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int thisDay = cal.get(Calendar.DAY_OF_YEAR);

            if (thisDay != dayOfYear) {
                numOfDays++;
                if (numOfDays >=3) {
                    break;
                }
                dayOfYear = thisDay;
                if (!(currentHeader.equals(""))) {
                    this.events.put(currentHeader, eventsInDay);
                    eventsInDay = new ArrayList<Article>();
                }
                SimpleDateFormat hFormat = new SimpleDateFormat("EEEE, MMMM d");
                String headerString = hFormat.format(date);
                this.eventHeaders.add(headerString);
                currentHeader = headerString;

            }
            eventsInDay.add(article);
        }
        if (eventsInDay.size() > 0) {
            this.events.put(currentHeader, eventsInDay);
        }

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TableLayout eventsBox = (TableLayout) v.findViewById(R.id.events_box);
        eventsBox.removeAllViews();
        for (String header: eventHeaders) {
            View headerRow = inflater.inflate(R.layout.events_list_header, eventsBox, false);

            TextView lblListHeader = (TextView) headerRow
                    .findViewById(R.id.header_title);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(header);

            eventsBox.addView(headerRow);

            List<Article> theseArticles = events.get(header);
            for (final Article article :theseArticles) {
                View row = inflater.inflate(R.layout.events_row, eventsBox, false);
                eventsBox.addView(row);

                TextView txtListChild = (TextView) row
                        .findViewById(R.id.row_title);
                TextView dateListChild = (TextView) row
                        .findViewById(R.id.row_time);
                ImageView disc_icon = (ImageView) row.findViewById(R.id.row_disc_icon);

                SimpleDateFormat df = new SimpleDateFormat("h:mm a");
                String dateString = df.format(article.date);
                if (dateString.equals("12:00 AM")) {
                    dateString = "All Day";
                }
                String titleString = article.title;

                dateListChild.setText(dateString);
                txtListChild.setText(titleString);

                View.OnClickListener cl = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Fragment newFragment = new EventsDetailFragment();
                        sendToDetailFragment(article, newFragment);
                    }
                };
                row.setOnClickListener(cl);
            }
        }

    }

    private void sendToDetailFragment(Article sendArticle, Fragment newFragment) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("detail_article", sendArticle);
        newFragment.setArguments(bundle);
        if (getActivity().findViewById(R.id.frame_container) != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_detail_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}

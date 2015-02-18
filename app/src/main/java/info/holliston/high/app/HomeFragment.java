package info.holliston.high.app;


import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import info.holliston.high.app.datamodel.Article;
import info.holliston.high.app.datamodel.download.ArticleDataSource;
import info.holliston.high.app.datamodel.download.ArticleParser;
import info.holliston.high.app.pager.DailyAnnPagerFragment;
import info.holliston.high.app.pager.EventPagerFragment;
import info.holliston.high.app.pager.LunchPagerFragment;
import info.holliston.high.app.pager.NewsPagerFragment;
import info.holliston.high.app.pager.SchedulePagerFragment;

public class HomeFragment extends android.support.v4.app.Fragment {

    private Article newsArticle;

    List<String> eventHeaders = new ArrayList<>();
    HashMap<String, List<Article>> events = new HashMap<>();
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.home_fragment,
                container, false);

        final SwipeRefreshLayout swipeLayout=(SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        final ScrollView scrollView = (ScrollView) v.findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                if(scrollY == 0) swipeLayout.setEnabled(true);
                else swipeLayout.setEnabled(false);

            }
        });
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                MainActivity ma = (MainActivity) getActivity();
                ma.refreshData(ArticleParser.SourceMode.PREFER_DOWNLOAD, ImageAsyncCacher.SourceMode.DOWNLOAD_ONLY);
            }
        });

        return v;
        }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateUI();
    }

    public void updateUI(){
        MainActivity ma = (MainActivity) getActivity();

        ArticleDataSource eventsSource = ma.eventsSource;
        ArticleDataSource newsSource = ma.newsSource;
        ArticleDataSource schedulesSource = ma.scheduleSource;
        ArticleDataSource dailyAnnSource = ma.dailyannSource;
        ArticleDataSource lunchSource = ma.lunchSource;

        List<Article> tempArticles;
        Article scheduleArticle;
        int schedIndex;
        Article dailyAnnArticle;
        Article lunchArticle;
        int lunchIndex;
        Date scheduleDate = new Date();
        List<Article> eventsArticles;

        tempArticles = newsSource.getAllArticles();

        if (tempArticles.size() >0) {
            newsArticle = tempArticles.get(0);
            assignNews(newsArticle);
        }

        /*Get the most recent schedule */
        tempArticles = schedulesSource.getAllArticles();

        if (tempArticles.size() >0) {
            scheduleArticle = tempArticles.get(0);
            schedIndex = 0;

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
                        schedIndex = 1;
                    }
                }
            }
            assignSchedule(scheduleArticle, schedIndex);
            scheduleDate = scheduleArticle.date;
        }

        /*Get the most lunch menu for that date */
        tempArticles = lunchSource.getAllArticles();

        if (tempArticles.size() >0) {
            lunchArticle = tempArticles.get(0);
            lunchIndex =0;
            for (int z = 0; z< tempArticles.size(); z++) {
                if (tempArticles.get(z).date.compareTo(scheduleDate) == 0) {
                    lunchArticle = tempArticles.get(z);
                    lunchIndex = z;
                }
            }
            assignLunch(lunchArticle, lunchIndex);
        }

        /*Get the most recent daily announcements */
        tempArticles = dailyAnnSource.getAllArticles();

        if (tempArticles.size() >0) {
            dailyAnnArticle = tempArticles.get(0);
            assignDailyAnn(dailyAnnArticle);
        }

        /*Get upcoming events */
        eventsArticles = eventsSource.getAllArticles();

        TableLayout eventsTable = (TableLayout) v.findViewById(R.id.events_box);
        if ((eventsTable.getChildCount() == 0) && (eventsArticles.size() >0)){
            assignEvents(eventsArticles);
        }
    }

    private void assignNews(Article article) {

        if (getActivity().findViewById(R.id.news_box) == null) {
            return;
        }
        TextView titlesTextView = (TextView) v.findViewById(R.id.news_title);
        titlesTextView.setText(article.title);

        String imgSrc = article.imgSrc;
        String key = article.key;

        ImageAsyncLoader.ViewHolder holder;
        ImageView imageView = (ImageView) v.findViewById(R.id.news_image);

        holder = new ImageAsyncLoader.ViewHolder();
        v.setTag(holder);

        holder.position = 0;

        holder.thumbnail = imageView;

        holder.thumbnail.setVisibility(View.INVISIBLE);

        holder.loading = (ProgressBar) v.findViewById(R.id.news_pbar);
        holder.loading.setVisibility(View.VISIBLE);

        if ((imgSrc != null) && (imgSrc.length() > 0)) {
            //imageDownloader.setMode(ImageDownloader.Mode.NO_DOWNLOADED_DRAWABLE);
            //imageDownloader.download(articleList.get(position).imgSrc, (ImageView) rowView.findViewById(R.id.row_icon));

            int newHeight = 250;
            int newWidth = imageView.getWidth();

            ImageAsyncLoader ial = new ImageAsyncLoader(0, holder,
                    newWidth, newHeight,
                    ImageAsyncLoader.FitMode.FULL,
                    ImageAsyncLoader.SourceMode.ALLOW_BOTH,
                    key, getActivity().getApplicationContext());
            //DownloadedDrawable downloadedDrawable = new DownloadedDrawable(ial);
            ial.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imgSrc);

        }

        View box = v.findViewById(R.id.news_box);
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewsPagerFragment newFragment = new NewsPagerFragment();
                sendToDetailFragment(0, newFragment, 2);
            }
        };
        box.setOnClickListener(cl);
    }

    private void assignSchedule(Article article, final int z) {

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
                imageView.setImageResource(R.drawable.a_lg);
                break;
            case 'B' :
                imageView.setImageResource(R.drawable.b_lg);
                break;
            case 'C' :
                imageView.setImageResource(R.drawable.c_lg);
                break;
            case 'D' :
                imageView.setImageResource(R.drawable.d_lg);
                break;
            default :
                imageView.setImageResource(R.drawable.star_lg);
                break;
        }

        View box = v.findViewById(R.id.schedule_box);
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SchedulePagerFragment newFragment = new SchedulePagerFragment();
                sendToDetailFragment(z, newFragment, 1);
            }
        };
        box.setOnClickListener(cl);
    }

    private void assignLunch(Article article, final int z) {

        TextView txtListChild = (TextView) v.findViewById(R.id.lunch_title);
        String titleString = "Lunch: "+ article.title;
        txtListChild.setText(titleString);

        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment newFragment = new LunchPagerFragment();
                sendToDetailFragment(z, newFragment, 5);
            }
        };
        txtListChild.setOnClickListener(cl);
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
                DailyAnnPagerFragment newFragment = new DailyAnnPagerFragment();
                sendToDetailFragment(0, newFragment, 3);
            }
        };
        box.setOnClickListener(cl);
    }

    private void assignEvents(List<Article> articles) {
        int dayOfYear = -1;
        String currentHeader = "";
        List<Article> eventsInDay = new ArrayList<>();
        int numOfDays = 0;
        eventHeaders = new ArrayList<>();
        events = new HashMap<>();

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
                    eventsInDay = new ArrayList<>();
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

            for (int i=0; i<theseArticles.size(); i++) { //(final Article article :theseArticles) {
                Article article = theseArticles.get(i);
                View row = inflater.inflate(R.layout.events_row, eventsBox, false);
                eventsBox.addView(row);

                TextView txtListChild = (TextView) row
                        .findViewById(R.id.row_title);
                TextView dateListChild = (TextView) row
                        .findViewById(R.id.row_time);
                ImageView disc_icon = (ImageView) row.findViewById(R.id.row_disc_icon);
                if (article.details.equals("")) {
                    disc_icon.setVisibility(ImageView.INVISIBLE);
                }
                SimpleDateFormat df = new SimpleDateFormat("h:mm a");
                String dateString = df.format(article.date);
                if (dateString.equals("12:00 AM")) {
                    dateString = "All Day";
                }
                String titleString = article.title;

                dateListChild.setText(dateString);
                txtListChild.setText(titleString);

                final int j = i;

                View.OnClickListener cl = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    EventPagerFragment newFragment = new EventPagerFragment();
                    sendToDetailFragment(j, newFragment, 4);
                    }
                };
                row.setOnClickListener(cl);

            }
        }

    }

    private void sendToDetailFragment(int i, Fragment newFragment, int tabPagerPosition) {

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
            //transaction.addToBackStack(null);
            transaction.commit();
            MainActivity ma = (MainActivity) getActivity();
            ma.tabPagerFragment.setPage(tabPagerPosition);
        }
    }
    public void showFirstNews() {
        if (getActivity().findViewById(R.id.frame_detail_container) != null) {
            if (newsArticle!=null) {
                sendToDetailFragment(0, new NewsPagerFragment(), 0);
            }
        }
    }
}

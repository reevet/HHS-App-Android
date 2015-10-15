package info.holliston.high.app.list.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import info.holliston.high.app.datamodel.download.ImageAsyncLoader;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.Article;

public class NewsCardAdapter extends RecyclerView.Adapter<NewsCardAdapter.NewsViewHolder> {
    private final Context context;
    private final List<Article> articleList;

    public NewsCardAdapter(Context context, List<Article> list) {
        this.context = context;
        this.articleList = list;
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    @Override
    public void onBindViewHolder(NewsViewHolder newsViewHolder, int position) {
        Article a = articleList.get(position);

        newsViewHolder.vTitle.setText(a.title);

        SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d");
        String dateString = df.format(a.date);
        newsViewHolder.vDate.setText(dateString);

        ImageAsyncLoader.ViewHolder holder = new ImageAsyncLoader.ViewHolder();

        holder.position = position;

        holder.thumbnail = newsViewHolder.vImage;
        holder.thumbnail.setVisibility(View.INVISIBLE);

        holder.loading = newsViewHolder.vProgress;
        holder.loading.setVisibility(View.VISIBLE);

        String imgSrc = articleList.get(position).imgSrc;
        if ((imgSrc != null) && (imgSrc.length() > 0)) {
            //imageDownloader.setMode(ImageDownloader.Mode.NO_DOWNLOADED_DRAWABLE);
            //imageDownloader.download(articleList.get(position).imgSrc, (ImageView) rowView.findViewById(R.id.row_icon));

            int newHeight = (int) context.getResources().getDimension(R.dimen.news_thumbnail_height);
            int newWidth = (int) context.getResources().getDimension(R.dimen.news_thumbnail_width);

            String key = articleList.get(position).key;
            ImageAsyncLoader ial = new ImageAsyncLoader(position, holder,
                    newWidth, newHeight,
                    ImageAsyncLoader.FitMode.FILL,
                    ImageAsyncLoader.SourceMode.ALLOW_BOTH,
                    key, context);
            //DownloadedDrawable downloadedDrawable = new DownloadedDrawable(ial);
            ial.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imgSrc);
        }

    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_row, parent, false);
        return new NewsViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }


    public class NewsViewHolder extends RecyclerView.ViewHolder {

        final TextView vTitle;
        final TextView vDate;
        final ImageView vImage;
        final ProgressBar vProgress;


        public NewsViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.row_title);
            vDate = (TextView) v.findViewById(R.id.row_date);
            vImage = (ImageView) v.findViewById(R.id.row_icon);
            vProgress = (ProgressBar) v.findViewById(R.id.row_progress);
        }


    }

}


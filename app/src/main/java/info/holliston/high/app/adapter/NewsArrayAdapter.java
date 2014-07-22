package info.holliston.high.app.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import info.holliston.high.app.R;
import info.holliston.high.app.model.Article;

public class NewsArrayAdapter extends ArrayAdapter<Article> {
    private final Context context;
    private final List<Article> articleList;
    //private final ImageDownloader imageDownloader = new ImageDownloader();

    public NewsArrayAdapter(Context context, List<Article> list) {
        super(context, R.layout.news_row, list);
        this.context = context;
        this.articleList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageAsyncLoader.ViewHolder holder;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.news_row, parent, false);

            holder = new ImageAsyncLoader.ViewHolder();

            convertView.setTag(holder);
        } else {
            holder = (ImageAsyncLoader.ViewHolder) convertView.getTag();
        }

        holder.position = position;

        holder.thumbnail = (ImageView) convertView.findViewById(R.id.row_icon);
        holder.thumbnail.setVisibility(View.INVISIBLE);

        holder.loading  = (ProgressBar) convertView.findViewById(R.id.row_progress);
        holder.loading.setVisibility(View.VISIBLE);

        TextView titlesTextView = (TextView) convertView.findViewById(R.id.row_title);
        titlesTextView.setText(articleList.get(position).title);

        SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d");
        String dateString = df.format(articleList.get(position).date);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.row_date);
        dateTextView.setText(dateString);

        String imgSrc = articleList.get(position).imgSrc;
        if ((imgSrc != null) && (imgSrc.length() >0)) {
            //imageDownloader.setMode(ImageDownloader.Mode.NO_DOWNLOADED_DRAWABLE);
            //imageDownloader.download(articleList.get(position).imgSrc, (ImageView) rowView.findViewById(R.id.row_icon));

            int newHeight = (int)context.getResources().getDimension(R.dimen.news_thumbnail);
            int newWidth = (int)context.getResources().getDimension(R.dimen.news_thumbnail);

            UUID key = articleList.get(position).key;
            ImageAsyncLoader ial = new ImageAsyncLoader (position, holder,
                    newWidth, newHeight,
                    ImageAsyncLoader.FitMode.FILL, ImageAsyncLoader.SourceMode.ALLOW_BOTH,
                    key, getContext());
            //DownloadedDrawable downloadedDrawable = new DownloadedDrawable(ial);
            ial.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imgSrc);
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }




}


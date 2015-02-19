package info.holliston.high.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import info.holliston.high.app.datamodel.Article;

/*
 * Downloads all images stored as URLs in the articleList provided
 */
public class ImageAsyncCacher extends AsyncTask<Void, Void, Void> {
    final String DEBUG_NAME = "ImageAysncCacher";
    int newWidth;
    int newHeight;
    Context context;

    private List<Article> articleList;

    public ImageAsyncCacher(int width, int height, Context c, List<Article> al) {
        this.newWidth = width;
        this.newHeight = height;
        this.context = c;
        this.articleList = al;
    }

    //get a scaling factor (to save memory)
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        //Log.d("imageCache", "inSampleSize =" + inSampleSize);
        return inSampleSize;
    }

    protected Void doInBackground(Void... arg0) {
        for (Article art : articleList) {
            String urldisplay = art.imgSrc;

            // Create an object to store file/download results in
            ImageLoadingObject ilo = new ImageLoadingObject();
            ilo.article = art;
            BitmapFactory.Options webImageOptions;

            //determine if the image is available for download
            webImageOptions = getWebImageExists(ilo, urldisplay);
            ilo.options = webImageOptions;

            if (webImageOptions != null) {
                try {
                    //get a scaling factor
                    ilo.options.inSampleSize = calculateInSampleSize(ilo.options, newWidth, newHeight);

                    // Decode bitmap with inSampleSize set
                    ilo.options.inJustDecodeBounds = false;
                    ilo.in = new URL(urldisplay).openStream();
                    ilo.fullImage = BitmapFactory.decodeStream(ilo.in, null, ilo.options);
                    File outfile = new File(this.context.getCacheDir(), ilo.article.key);
                    ilo.os = new FileOutputStream(outfile);
                    ilo.fullImage.compress(Bitmap.CompressFormat.PNG, 100, ilo.os);
                    try {
                        ilo.in.close();

                    } catch (Throwable ignore) {
                    }
                    try {
                        ilo.os.close();
                    } catch (Throwable ignore) {
                    }
                } catch (Exception x) {
                    Log.e(DEBUG_NAME, "Error with bitmaps: " + x.toString());
                }
            }
        }
        return null;
    }

    protected BitmapFactory.Options getWebImageExists(ImageLoadingObject ilo, String urldisplay) {
        try {
            ilo.in = new URL(urldisplay).openStream();
            ilo.options = new BitmapFactory.Options();
            ilo.options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(ilo.in, null, ilo.options);
        } catch (Exception ex) {
            //Log.e(DEBUG_NAME, "Error downloading image " + ilo.article.title);
        }
        try {
            ilo.in.close();
        } catch (Exception e) {
            //
        }
        return ilo.options;
    }

    /*
     * Rounds the corners of images. No longer used, but I worked so hard to
     * create the method that I hate to delete it.  :)
     */
    /*public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        //final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, pixels, pixels, paint);
        //canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }*/

    /*
     * An object to holder the filein/out stuff
     */
    public static class ImageLoadingObject {
        Bitmap fullImage = null;
        BitmapFactory.Options options = null;
        InputStream in = null;
        FileOutputStream os = null;
        Article article;
    }

}
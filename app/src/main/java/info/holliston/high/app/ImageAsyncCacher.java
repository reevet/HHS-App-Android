package info.holliston.high.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import info.holliston.high.app.datamodel.Article;

public class ImageAsyncCacher extends AsyncTask<List<Article>, Void, Void> {
    final String DEBUG_NAME = "ImageAysncCacher";
    int newWidth;
    int newHeight;
    Context context;
    public static enum SourceMode {DOWNLOAD_ONLY, ALLOW_BOTH}

    private SourceMode sourceMode;

    public ImageAsyncCacher(int width, int height, SourceMode sourceMode, Context c) {
        this.newWidth = width;
        this.newHeight = height;
        this.sourceMode = sourceMode;
        this.context = c;
    }

    protected Void doInBackground(List<Article>... lists) {
        List<Article> articleList = lists[0];
        for (Article art : articleList) {

            String urldisplay = art.imgSrc;

            ImageLoadingObject ilo = new ImageLoadingObject();
            ilo.article = art;
            BitmapFactory.Options cachedImageOptions = null;
            BitmapFactory.Options webImageOptions = null;

            webImageOptions = getWebImageExists(ilo, urldisplay);
            ilo.options = webImageOptions;

            if ((SourceMode.DOWNLOAD_ONLY != this.sourceMode)) {
                cachedImageOptions = getCachedImageExists(ilo);
                ilo.options = cachedImageOptions;
            }

            if ((cachedImageOptions != null) || (webImageOptions != null)) {
                try {
                    int oldHeight = ilo.options.outHeight;
                    int oldWidth = ilo.options.outWidth;

                    ilo.options.inSampleSize = calculateInSampleSize(ilo.options, newWidth, newHeight);
                    Float ratio = (float) 1;

                    int scaledWidth = Math.round(oldWidth / ratio);
                    int scaledHeight = Math.round(oldHeight / ratio);

                    int ox = scaledWidth / 2 - this.newWidth / 2;
                    int oy = scaledHeight / 2 - this.newHeight / 2;

                    // Decode bitmap with inSampleSize set
                    ilo.options.inJustDecodeBounds = false;
                    if (webImageOptions != null) {
                        ilo.in = new URL(urldisplay).openStream();
                        ilo.fullImage = BitmapFactory.decodeStream(ilo.in, null, ilo.options);
                    } else if (cachedImageOptions != null) {
                        ilo.filein = new File(context.getCacheDir(), ilo.article.key);
                        ilo.fis = new FileInputStream(ilo.filein);
                        ilo.fullImage = BitmapFactory.decodeFile(ilo.filein.getAbsolutePath(), ilo.options);
                    }
                    Bitmap tempImage = Bitmap.createScaledBitmap(ilo.fullImage, scaledWidth, scaledHeight, false);
                    ilo.fittedImage = ilo.fullImage;

                } catch (Exception x) {
                    Log.e(DEBUG_NAME, "Error with bitmaps: " + x.toString());
                }
            }

            if (webImageOptions != null) {
                try {
                    File outfile = new File(this.context.getCacheDir(), ilo.article.key);
                    ilo.os = new FileOutputStream(outfile);
                    ilo.fullImage.compress(Bitmap.CompressFormat.PNG, 100, ilo.os);
                    Log.i(DEBUG_NAME, "Successfully cached image for " + ilo.article .title);
                } catch (Exception ex) {
                    Log.e(DEBUG_NAME, "Error writing image to cache");
                } finally {
                    try {
                        ilo.os.close();
                    } catch (Throwable ignore) {
                    }
                    try {
                        ilo.in.close();
                    } catch (Throwable ignore) {
                    }
                }

            } else if (cachedImageOptions != null) {
                try {
                    ilo.fis.close();
                } catch (Throwable ignore) {
                }
            }
        }
        return null;
    }

    protected Void onPostExecute() {
        return null;
    }

    protected BitmapFactory.Options getCachedImageExists(ImageLoadingObject ilo) {
        try {
            ilo.filein = new File(context.getCacheDir(), ilo.article.key);
            //File testFile = new File(context.getCacheDir().getPath());
            //File[] files = testFile.listFiles();
            ilo.fis = new FileInputStream(ilo.filein);
            ilo.options = new BitmapFactory.Options();
            BitmapFactory.decodeStream(ilo.fis, null, ilo.options);

        } catch (Exception e) {
            Log.d(DEBUG_NAME, "cached image not found");
        }
        try {
            ilo.fis.close();
        } catch (Exception e) {
            //
        }
        return ilo.options;
    }

    protected BitmapFactory.Options getWebImageExists(ImageLoadingObject ilo, String urldisplay) {
        try {
            ilo.in = new URL(urldisplay).openStream();
            ilo.options = new BitmapFactory.Options();
            ilo.options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(ilo.in, null, ilo.options);
        } catch (Exception ex) {
            Log.e(DEBUG_NAME, "Error downloading image " + ilo.article.title);
        }
        try {
            ilo.in.close();
        } catch (Exception e){
            //
        }
        return ilo.options;
    }

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

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
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
    }

    public static class ImageLoadingObject {
        Bitmap fittedImage = null;
        Bitmap fullImage = null;
        BitmapFactory.Options options = null;
        InputStream in = null;
        File filein = null;
        FileInputStream fis = null;
        FileOutputStream os = null;
        String iFound = "";
        int oldHeight = 0;
        int oldWidth = 0;
        Article article;
    }

}
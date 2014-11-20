package info.holliston.high.app.adapter;

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
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class ImageAsyncLoader extends AsyncTask<String, Void, Bitmap> {
    //private final WeakReference<ImageView> bmImage;
    //private final WeakReference<ProgressBar> pbar;
    int newWidth;
    int newHeight;
    Context context;
    UUID key;
    private int mPosition;
    private ViewHolder mHolder;

    public static enum FitMode {FIT, FILL, FULL, MATCH}
    public static enum SourceMode {CACHE_ONLY, DOWNLOAD_ONLY, ALLOW_BOTH}

    private FitMode fitMode;
    private SourceMode sourceMode;

    public ImageAsyncLoader(int position, ViewHolder holder,
                            int width, int height, FitMode fitMode, SourceMode sourceMode,
                            UUID key, Context c) {
        this.mPosition = position;
        this.mHolder = holder;
        this.newWidth = width;
        this.newHeight = height;
        this.key = key;
        this.context = c;
        this.fitMode = fitMode;
        this.sourceMode = sourceMode;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];

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

        if ((SourceMode.DOWNLOAD_ONLY != this.sourceMode)) {
            try {
                filein = new File(context.getCacheDir(), key.toString());
                //File testFile = new File(context.getCacheDir().getPath());
                //File[] files = testFile.listFiles();
                fis = new FileInputStream(filein);
                options = new BitmapFactory.Options();
                BitmapFactory.decodeStream(fis, null, options);

                oldHeight = options.outHeight;
                oldWidth = options.outWidth;
                if ((oldHeight > 0) && (oldWidth > 0)) {
                    iFound = "cache";
                } else {
                    Log.d("imageCache", "cached image file had dimensions 0x0");
                }
            } catch (Exception e) {
                Log.d("imageCache", "cached image file had dimensions 0x0");
            }
        }
        if (mPosition != mHolder.position) {
            return null;
        }

        if (!(iFound.equals("cache")) &&
                (this.sourceMode != SourceMode.CACHE_ONLY) ) {
            try {
                in = new URL(urldisplay).openStream();
                options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, options);

                oldHeight = options.outHeight;
                oldWidth = options.outWidth;
                if ((oldHeight > 0) && (oldWidth > 0)) {
                    iFound = "web";
                } else {
                    Log.d("imageCache", "web image file had dimensions 0x0");
                }
            } catch (Exception ex) {
                iFound = "";
            }
        }
        if (mPosition != mHolder.position) {
            return null;
        }
        if (!(iFound).equals("")) {
            try {
                options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);

                Float ratio = (float) 1;
                if (fitMode == FitMode.FIT) {
                    ratio = Math.max((float) oldHeight / newHeight, (float) oldWidth / newWidth);
                } else if (fitMode == FitMode.FILL) {
                    ratio = Math.min((float) oldHeight / newHeight, (float) oldWidth / newWidth);
                }

                int scaledWidth = Math.round(oldWidth / ratio);
                int scaledHeight = Math.round(oldHeight / ratio);

                int ox = scaledWidth / 2 - this.newWidth / 2;
                int oy = scaledHeight / 2 - this.newHeight / 2;

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                if (iFound.equals("web")) {
                    in = new URL(urldisplay).openStream();
                    fullImage = BitmapFactory.decodeStream(in, null, options);
                } else if (iFound.equals("cache")) {
                    fullImage = BitmapFactory.decodeFile(filein.getAbsolutePath(), options);
                }
                Bitmap tempImage = Bitmap.createScaledBitmap(fullImage, scaledWidth, scaledHeight, false);
                Log.d("imageCacheDim",ox+", "+oy+", "+newWidth+", "+newHeight);
                Log.d("imageCacheDim", "scaled: "+scaledWidth+", "+scaledHeight);
                Log.d("imageCacheDim", "----------------");
                if (fitMode == FitMode.FILL) {
                    fittedImage = Bitmap.createBitmap(tempImage, ox, oy, newWidth, newHeight);
                } else if (fitMode == FitMode.FIT) {
                    fittedImage = tempImage;
                } else if (fitMode == FitMode.FULL) {
                    fittedImage = fullImage;
                }
            } catch (Exception x) {
                Log.d("imageCache", "Error with bitmaps: " + x.toString());
            }
        }
        if (mPosition != mHolder.position) {
            return null;
        }

        if (iFound.equals("web")) {
            try {
                File outfile = new File(this.context.getCacheDir(), key.toString());
                os = new FileOutputStream(outfile);
                fullImage.compress(Bitmap.CompressFormat.PNG, 60, os);
            } catch (Exception ex) {
                Log.d("imageCache", "Error writing image to cache");
            } finally {
                try {
                    os.close();
                } catch (Throwable ignore) {
                }
                try {
                    in.close();
                } catch (Throwable ignore) {
                }
            }

        } else if (iFound.equals("cache")) {
            try {
                fis.close();
            } catch (Throwable ignore) {
            }
        }
        return fittedImage;
    }

    protected void onPostExecute(Bitmap result) {
        //try {
        if ((mPosition == mHolder.position) && (result != null)) {

            //mHolder.thumbnail.setImageBitmap(getRoundedCornerBitmap(result, 20));
            mHolder.thumbnail.setImageBitmap(result);
            mHolder.thumbnail.setVisibility(View.VISIBLE);
            mHolder.loading.setVisibility(View.INVISIBLE);
            //} catch (Exception e) {
            //    Log.e("imageCache", "Error assigning bitmap: "+e.toString());
            //}
        }
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
        Log.d("imageCache", "inSampleSize =" + inSampleSize);
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

    public static class ViewHolder {
        public int position;
        public ImageView thumbnail;
        public ProgressBar loading;
    }

}
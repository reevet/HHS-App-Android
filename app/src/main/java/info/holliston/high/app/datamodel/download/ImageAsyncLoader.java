package info.holliston.high.app.datamodel.download;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

/*
 * Adds image async to a holder, downloading if necessary
 */
public class ImageAsyncLoader extends AsyncTask<String, Void, Bitmap> {
    private final String DEBUG_NAME = "ImageAsyncLoader";
    private final int newWidth;
    private final int newHeight;
    private final Context context;
    private final String key;
    private int mPosition;
    private ViewHolder mHolder;
    private FitMode fitMode;
    private SourceMode sourceMode;

    public ImageAsyncLoader(int position, ViewHolder holder,
                            int width, int height, FitMode fitMode, SourceMode sourceMode,
                            String key, Context c) {
        this.mPosition = position;
        this.mHolder = holder;
        this.newWidth = width;
        this.newHeight = height;
        this.key = key;
        this.context = c;
        this.fitMode = fitMode;
        this.sourceMode = sourceMode;
    }

    // get a scaling factor, to save memory
    private static int calculateInSampleSize(
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
        return inSampleSize;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        ImageLoadingObject ilo = new ImageLoadingObject();
        BitmapFactory.Options cachedImageOptions = null;
        BitmapFactory.Options webImageOptions = null;

        //get cachedImage dimensions
        if ((SourceMode.DOWNLOAD_ONLY != this.sourceMode)) {
            cachedImageOptions = getCachedImageExists(ilo);
            ilo.options = cachedImageOptions;
        }
        //bail out of the mHolder (imagebox) is no longer on the screen
        if (mPosition != mHolder.position) {
            return null;
        }

        //get downloaded image dimensions from the web
        if ((cachedImageOptions == null) && (this.sourceMode != SourceMode.CACHE_ONLY)) {
            webImageOptions = getWebImageExists(ilo, urldisplay);
            ilo.options = webImageOptions;
        }
        if (mPosition != mHolder.position) {
            return null;
        }

        //if an image exists in either place...
        if ((cachedImageOptions != null) || (webImageOptions != null)) {
            try {
                //get the dimensions of the web or cached image
                int oldHeight = ilo.options.outHeight;
                int oldWidth = ilo.options.outWidth;

                //get a scaling factor (to save memory)
                ilo.options.inSampleSize = calculateInSampleSize(ilo.options, newWidth, newHeight);

                //get new dimensions based on fitting or filling the available space
                Float ratio = 1.0f;
                if (fitMode == FitMode.FIT) {
                    ratio = Math.max((float) oldHeight / newHeight, (float) oldWidth / newWidth);
                } else if (fitMode == FitMode.FILL) {
                    ratio = Math.min((float) oldHeight / newHeight, (float) oldWidth / newWidth);
                }
                int scaledWidth = Math.round(oldWidth / ratio);
                int scaledHeight = Math.round(oldHeight / ratio);

                //move top left corner if the image is bigger than available space
                int ox = scaledWidth / 2 - this.newWidth / 2;
                int oy = scaledHeight / 2 - this.newHeight / 2;

                // Decode bitmap with inSampleSize set
                ilo.options.inJustDecodeBounds = false;
                if (webImageOptions != null) {
                    ilo.in = new URL(urldisplay).openStream();
                    ilo.fullImage = BitmapFactory.decodeStream(ilo.in, null, ilo.options);
                    Log.d("IALoader", "Downloading image:" + urldisplay);
                    try {
                        ilo.in.close();
                    } catch (Throwable ignore) {
                    }
                } else {
                    ilo.filein = new File(context.getCacheDir(), key);
                    ilo.fis = new FileInputStream(ilo.filein);
                    ilo.fullImage = BitmapFactory.decodeFile(ilo.filein.getAbsolutePath(), ilo.options);
                    try {
                        ilo.fis.close();
                    } catch (Throwable ignore) {
                    }
                }
                //create a scaled and cropped image
                Bitmap tempImage = Bitmap.createScaledBitmap(ilo.fullImage, scaledWidth, scaledHeight, false);
                if (fitMode == FitMode.FILL) {
                    ilo.fittedImage = Bitmap.createBitmap(tempImage, ox, oy, newWidth, newHeight);
                } else if (fitMode == FitMode.FIT) {
                    ilo.fittedImage = tempImage;
                } else if (fitMode == FitMode.FULL) {
                    ilo.fittedImage = ilo.fullImage;
                }
            } catch (Exception x) {
                Log.e(DEBUG_NAME, "Error with bitmaps: " + x.toString());
            }
        }
        if (mPosition != mHolder.position) {
            return null;
        }

        if ((webImageOptions != null) && (sourceMode != SourceMode.CACHE_ONLY)) {
            try {
                File outfile = new File(this.context.getCacheDir(), key);
                ilo.os = new FileOutputStream(outfile);
                ilo.fullImage.compress(Bitmap.CompressFormat.PNG, 100, ilo.os);
                Log.i(DEBUG_NAME, "Successfully cached image with key " + key);
            } catch (Exception ex) {
                Log.e(DEBUG_NAME, "Error writing image to cache");
            } finally {
                try {
                    ilo.os.close();
                } catch (Throwable ignore) {
                }
            }
        }
        return ilo.fittedImage;
    }

    protected void onPostExecute(Bitmap result) {
        //put the image into the holder
        if ((mPosition == mHolder.position) && (result != null)) {
            mHolder.thumbnail.setImageBitmap(result);
            mHolder.thumbnail.setVisibility(View.VISIBLE);
            mHolder.loading.setVisibility(View.INVISIBLE);
        }
    }

    //get the dimensions of the cached image, if it exists
    BitmapFactory.Options getCachedImageExists(ImageLoadingObject ilo) {
        try {
            ilo.filein = new File(context.getCacheDir(), key);
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

    // get the image dimensions from the web image, if available
    BitmapFactory.Options getWebImageExists(ImageLoadingObject ilo, String urldisplay) {
        try {
            ilo.in = new URL(urldisplay).openStream();
            ilo.options = new BitmapFactory.Options();
            ilo.options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(ilo.in, null, ilo.options);
        } catch (Exception ex) {
            //Log.e(DEBUG_NAME, "Error downloading image " + key);
        }
        try {
            ilo.in.close();
        } catch (Exception e) {
            //
        }
        return ilo.options;
    }

    // FIT = shrink image to fit completely inside
    // FILL = expand image to fill, possibly cropping edges
    // FULL = get full-sized image
    // MATCH = force image to the dimensions
    public static enum FitMode {
        FIT, FILL, FULL, MATCH
    }

    public static enum SourceMode {CACHE_ONLY, DOWNLOAD_ONLY, ALLOW_BOTH}

    /*
     * Rounds corners of images. No longer used, but I worked too hard on it to delete it.
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
     * A basic holder for the image to go in
     */
    public static class ViewHolder {
        public int position;
        public ImageView thumbnail;
        public ProgressBar loading;
    }

    /*
     * An object to hold the image file in/out stuff
     */
    public static class ImageLoadingObject {
        Bitmap fittedImage = null;
        Bitmap fullImage = null;
        BitmapFactory.Options options = null;
        InputStream in = null;
        File filein = null;
        FileInputStream fis = null;
        FileOutputStream os = null;
    }

}
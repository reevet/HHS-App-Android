package info.holliston.high.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;

import java.util.List;

public class SocialFragment extends Fragment {

    private static Intent intent;
    private static Context context;
    private static String facebookId;
    private static String twitterId;
    private static String instagramId;
    private static String youtubeId;
    private static String googleplusId;

    private static Intent getOpenFacebookIntent() {
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://faceweb/f/?href=https://www.facebook.com/" + facebookId));
        intent.setPackage("com.facebook.katana");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            return intent;
        } else {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + facebookId));
        }
    }

    private static Intent getOpenTwitterIntent() {
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitterId));
        intent.setPackage("com.twitter.android");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            return intent;
        } else {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + twitterId));
        }
    }

    private static Intent getOpenGoogleplusIntent() {
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/" + googleplusId + "/posts"));
        intent.setPackage("com.google.android.plus");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            return intent;
        } else {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/" + googleplusId + "/posts"));
        }
    }

    private static Intent getOpenInstagramIntent() {
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/" + twitterId));
        intent.setPackage("com.instagram.android");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            return intent;
        } else {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/" + instagramId));
        }
    }

    private static Intent getOpenYoutubeIntent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/" + youtubeId));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.social_table, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        context = getActivity().getApplicationContext();

        facebookId = getResources().getString(R.string.hhs_facebook_id);
        twitterId = getResources().getString(R.string.hhs_twitter_id);
        googleplusId = getResources().getString(R.string.hhs_googleplus_id);
        instagramId = getResources().getString(R.string.hhs_instagram_id);
        youtubeId = getResources().getString(R.string.hhs_youtube_id);

        TableRow facebookRow = (TableRow) getActivity().findViewById(R.id.facebook_row);
        TableRow twitterRow = (TableRow) getActivity().findViewById(R.id.twitter_row);
        TableRow googleplusRow = (TableRow) getActivity().findViewById(R.id.googleplus_row);
        TableRow instagramRow = (TableRow) getActivity().findViewById(R.id.instagram_row);
        TableRow youtubeRow = (TableRow) getActivity().findViewById(R.id.youtube_row);

        View.OnClickListener facebookListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = getOpenFacebookIntent();
                startActivity(intent);
            }
        };
        View.OnClickListener twitterListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = getOpenTwitterIntent();
                startActivity(intent);
            }
        };
        View.OnClickListener googleplusListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = getOpenGoogleplusIntent();
                startActivity(intent);
            }
        };
        View.OnClickListener instagramListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = getOpenInstagramIntent();
                startActivity(intent);
            }
        };
        View.OnClickListener youtTubeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = getOpenYoutubeIntent();
                startActivity(intent);
            }
        };
        facebookRow.setOnClickListener(facebookListener);
        twitterRow.setOnClickListener(twitterListener);
        googleplusRow.setOnClickListener(googleplusListener);
        instagramRow.setOnClickListener(instagramListener);
        youtubeRow.setOnClickListener(youtTubeListener);
    }


}

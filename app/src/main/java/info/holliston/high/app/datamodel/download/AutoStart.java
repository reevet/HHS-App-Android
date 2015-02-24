package info.holliston.high.app.datamodel.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by reevet on 2/24/2015.
 */
public class AutoStart extends BroadcastReceiver {

    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0.getApplicationContext(), ArticleDownloaderService.class);

        intent.putExtra("refreshSource", ArticleParser.SourceMode.PREFER_DOWNLOAD);
        intent.putExtra("getImages", "ALLOW_BOTH");
        //intent.putExtra("alarmReset", "resetAll");

        arg0.startService(intent);
        Log.d("MainActivity", "Refresh intent sent to ArticleDownloaderService");
        //Toast.makeText(arg0, "AutoStart started", Toast.LENGTH_LONG).show();

    }
}

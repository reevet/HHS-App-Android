package info.holliston.high.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Window;

import info.holliston.high.app.xmlparser.ArticleParser;

public class SplashActivity extends Activity {

    ArticleParser.SourceMode refreshSource;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                //Toast.makeText(SplashActivity.this, "Data sync complete", Toast.LENGTH_LONG).show();
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent i = getIntent();
        refreshSource = (ArticleParser.SourceMode) i.getSerializableExtra("refreshSource");
        if (refreshSource == null) {
            refreshSource = ArticleParser.SourceMode.PREFER_DOWNLOAD;
        }

        Intent intent = new Intent(getApplicationContext(), ArticleDownloaderService.class);
        intent.putExtra("refreshSource", refreshSource);
        startService(intent);

        setContentView(R.layout.splash);
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(ArticleDownloaderService.NOTIFICATION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
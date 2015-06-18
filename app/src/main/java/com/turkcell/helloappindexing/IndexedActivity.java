package com.turkcell.helloappindexing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class IndexedActivity extends AppCompatActivity {

    private static final String TAG = "IndexedActivity";

    private String meyve;

    private static final Uri BASE_APP_URI = Uri.parse("android-app://com.turkcell.helloappindexing/http/meyve-app.com/meyve/");

    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indexed);

        meyve = getIntent().getStringExtra("meyve");

        onNewIntent(getIntent());

        TextView txtValue = (TextView) findViewById(R.id.txt_value);
        txtValue.setText(meyve);

        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.APP_INDEX_API).build();
    }

    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            meyve = data.substring(data.lastIndexOf("/") + 1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(meyve)) {
            mClient.connect();

            final Uri APP_URI = BASE_APP_URI.buildUpon().appendPath(meyve).build();

            Action viewAction = Action.newAction(Action.TYPE_VIEW, meyve, APP_URI);

            // Call the App Indexing API view method
            PendingResult<Status> result = AppIndex.AppIndexApi.start(mClient, viewAction);

            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "App Indexing API: Recorded recipe " + meyve + " view successfully.");
                    } else {
                        Log.e(TAG, "App Indexing API: There was an error recording the recipe view." + status.toString());
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        if (!TextUtils.isEmpty(meyve)) {
            final Uri APP_URI = BASE_APP_URI.buildUpon().appendPath(meyve).build();
            Action viewAction = Action.newAction(Action.TYPE_VIEW, meyve, APP_URI);
            PendingResult<Status> result = AppIndex.AppIndexApi.end(mClient, viewAction);

            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "App Indexing API: Recorded recipe " + meyve + " view end successfully.");
                    } else {
                        Log.e(TAG, "App Indexing API: There was an error recording the recipe view." + status.toString());
                    }
                }
            });

            mClient.disconnect();
        }

        super.onStop();
    }
}

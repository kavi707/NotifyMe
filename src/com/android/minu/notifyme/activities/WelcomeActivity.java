package com.android.minu.notifyme.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.minu.notifyme.R;
import com.android.minu.notifyme.services.ActivityUserPermissionServices;


public class WelcomeActivity extends Activity {

    protected static final int TIMER_RUNTIME = 20000;
    protected boolean mbActive;
    private boolean isActivityActivated = false;

    final Context context = this;
    private Handler mHandler;

    private ProgressBar progressBar;
    private TextView progressText;

    private AlertDialog messageBalloonAlertDialog;

    private ActivityUserPermissionServices userPermissionServices = new ActivityUserPermissionServices();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        setUpViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityActivated = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    private void setUpViews() {

        progressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        progressText = (TextView) findViewById(R.id.progressBarTitleTextView);
        mHandler = new Handler();

        final Thread timerThread = new Thread() {
            @Override
            public void run() {
                mbActive = true;
                int progress = 0;

                try {
                    int waited = 0;
                    while (mbActive && (waited < TIMER_RUNTIME)) {
                        sleep(200);

                        if (mbActive) {
                            waited += 200;
                            progress = updateProgress(waited);

                            switch (progress) {
                                case 0:
                                    progressText.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressText.setText("Loading configurations ...");
                                        }
                                    });
                                    break;
                                case 20:
                                    progressText.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressText.setText("Check the Internet connection ...");
                                        }
                                    });
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (!userPermissionServices.isOnline(WelcomeActivity.this)){
                                                mbActive = false;
                                                messageBalloonAlertDialog = new AlertDialog.Builder(context)
                                                        .setTitle(R.string.warning)
                                                        .setMessage(R.string.offLineBalloonMsg)
                                                        .setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                progressBar.setProgress(100);
                                                                onContinue();
                                                            }
                                                        })
                                                        .setNeutralButton(R.string.action_settings, new AlertDialog.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                                                                finish();
                                                            }
                                                        })
                                                        .setNegativeButton(R.string.no, new AlertDialog.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                finish();
                                                            }
                                                        }).create();
                                                if (isActivityActivated) {
                                                    messageBalloonAlertDialog.show();
                                                }
                                            } else {
                                                //TODO: Doing the syncing stuff
                                            }
                                        }
                                    });
                                    break;
                                case 50:
                                    progressText.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressText.setText("Ready the application ...");
                                        }
                                    });
                                    break;
                                case 80:
                                    progressText.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressText.setText("Finalizing ...");
                                        }
                                    });
                                    break;
                                case 100:
                                    onContinue();
                                    break;
                            }
                            progressBar.setProgress(progress);
                        }
                    }
                } catch (InterruptedException ex) {
                    // do nothing
                }
            }
        };
        timerThread.start();
    }

    private int updateProgress(final int timePassed) {
        if (null != progressBar) {
            // Ignore rounding error here
            final int progress = progressBar.getMax() * timePassed / TIMER_RUNTIME;
            return progress;
        }
        return 0;
    }

    private void onContinue() {
        Intent notifierIntent = new Intent(WelcomeActivity.this, NotifyMeActivity.class);
        startActivity(notifierIntent);
        finish();
    }
}

package com.kavi.droid.notifyme.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kavi.droid.notifyme.R;
import com.kavi.droid.notifyme.services.ActivityUserPermissionServices;


/**
 * Created by Kavi on 11/5/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
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

    private void setUpViews() {

        // Initialise UI object from activity_welcome.xml
        progressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        progressText = (TextView) findViewById(R.id.progressBarTitleTextView);

        // Handler object for access UI objects in separate thread
        mHandler = new Handler();

        // Run separate thread for show the loading progress bar
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

    /**
     * This method for set progress value to progress bar against the given TIMER_RUNTIME
     * @param timePassed
     * @return Integer progress
     */
    private int updateProgress(final int timePassed) {
        if (null != progressBar) {
            // Ignore rounding error here
            final int progress = progressBar.getMax() * timePassed / TIMER_RUNTIME;
            return progress;
        }
        return 0;
    }

    /**
     * Process @ after complete the loading
     * Open the next view intent
     */
    private void onContinue() {
        Intent notifierIntent = new Intent(WelcomeActivity.this, NotifyMeActivity.class);
        startActivity(notifierIntent);
        finish();
    }
}

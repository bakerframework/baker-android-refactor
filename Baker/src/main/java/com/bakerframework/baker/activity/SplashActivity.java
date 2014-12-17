package com.bakerframework.baker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.HandlerThread;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.model.IssueCollection;
import com.bakerframework.baker.model.IssueCollectionListener;

/* @TODO: Provide some meaning to the loading screen */

public class SplashActivity extends Activity implements IssueCollectionListener {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);

        // Prepare issue collection
        final IssueCollection issueCollection = BakerApplication.getInstance().getIssueCollection();
        issueCollection.addListener(this);

        // Load shelf from cache if available
        if(issueCollection.isCacheAvailable()) {
            issueCollection.processManifestFileFromCache();
        }

        if (BakerApplication.getInstance().isNetworkConnected()) {
            // Reload issue collection if internet connection is available
            issueCollection.reload();
        }else if(!issueCollection.isCacheAvailable()) {
            onIssueCollectionLoadError();
        }

	}


    @Override
    public void onIssueCollectionLoaded() {

        // Remove listener
        BakerApplication.getInstance().getIssueCollection().removeListener(this);

        // Animate logo
        Animation bounceAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.bounce);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.logo_fade_in);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(bounceAnimation);
        animationSet.addAnimation(fadeInAnimation);
        ((ImageView) findViewById(R.id.spash_logo)).startAnimation(animationSet);

        // Wait for a little while for the shelf to load
        CountDownTimer timer = new CountDownTimer(SPLASH_TIME_OUT, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                // Launch shelf activity
                Intent i = new Intent(SplashActivity.this, ShelfActivity.class);
                startActivity(i);

                // Close Splash
                // SplashActivity.this.finish();
            }
        };
        timer.start();

    }

    @Override
    public void onIssueCollectionLoadError() {
        // No internet connection and no cached file
        new AlertDialog.Builder(this)
            .setTitle(this.getString(R.string.exit))
            .setMessage(this.getString(R.string.no_shelf_no_internet))
            .setPositiveButton(this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SplashActivity.this.finish();
                }
            })
            .show();
    }
}

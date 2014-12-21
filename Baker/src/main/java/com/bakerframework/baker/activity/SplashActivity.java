package com.bakerframework.baker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.model.IssueCollection;
import com.bakerframework.baker.model.IssueCollectionListener;

import com.bakerframework.baker.play.LicenceManagerDelegate;

/* @TODO: Provide some meaning to the loading screen */

public class SplashActivity extends Activity implements IssueCollectionListener, LicenceManagerDelegate {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);

        // Animate logo
        animateLogo();

        // Licence check
        if(getResources().getBoolean(R.bool.enable_licence_check)) {
            // Check for valid application licence (loadIssueCollection will be called afterwards
            BakerApplication.getInstance().getLicenceManager().setDelegate(this);
            BakerApplication.getInstance().getLicenceManager().checkAccess();
        }else{
            // Directly load data
            loadIssueCollection();
        }

	}

    private void animateLogo() {
        Animation bounceAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.bounce);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.logo_fade_in);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(bounceAnimation);
        animationSet.addAnimation(fadeInAnimation);
        findViewById(R.id.spash_logo).startAnimation(animationSet);
    }

    public void loadIssueCollection() {

        // Prepare issue collection
        final IssueCollection issueCollection = BakerApplication.getInstance().getIssueCollection();
        issueCollection.addListener(SplashActivity.this);

        if (BakerApplication.getInstance().isNetworkConnected()) {
            BakerApplication.getInstance().setApplicationMode(BakerApplication.APPLICATION_MODE_ONLINE);
            // Reload issue collection if internet connection is available
            issueCollection.reload();
        }else{
            BakerApplication.getInstance().setApplicationMode(BakerApplication.APPLICATION_MODE_OFFLINE);
            if(issueCollection.isCacheAvailable()) {
                issueCollection.processManifestFileFromCache();
            }else{
                onIssueCollectionLoadError();
            }
        }

    }

    @Override
    public void onIssueCollectionLoaded() {

        // Remove listener
        BakerApplication.getInstance().getIssueCollection().removeListener(this);

        // Wait for a little while for the shelf to load
        CountDownTimer timer = new CountDownTimer(getResources().getInteger(R.integer.splash_time_out), 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                // Launch shelf activity
                Intent i = new Intent(SplashActivity.this, ShelfActivity.class);
                startActivity(i);

                // Close Splash
                SplashActivity.this.finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Licensing

    @Override
    public void onLicenceValid(int reason) {
        loadIssueCollection();
    }

    @Override
    public void onLicenceInvalid(int reason) {
        // Invalid licence
        new AlertDialog.Builder(SplashActivity.this)
                .setTitle(getString(R.string.exit))
                .setMessage(getString(R.string.invalid_license_error))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SplashActivity.this.finish();
                    }
                })
                .show();
    }

    @Override
    public void onLicenceRetry(int reason) {
        new AlertDialog.Builder(SplashActivity.this)
                .setTitle(getString(R.string.exit))
                .setMessage(getString(R.string.no_shelf_no_internet))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SplashActivity.this.finish();
                    }
                })
                .show();
    }
}

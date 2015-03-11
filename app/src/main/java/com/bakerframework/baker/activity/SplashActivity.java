/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 * Copyright (c) 2015. Tobias Strebitzer, Francisco Contreras, Holland Salazar.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * Neither the name of the Baker Framework nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package com.bakerframework.baker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.events.IssueCollectionErrorEvent;
import com.bakerframework.baker.events.IssueCollectionLoadedEvent;
import com.bakerframework.baker.model.IssueCollection;

import com.bakerframework.baker.play.LicenceManagerDelegate;

import de.greenrobot.event.EventBus;

public class SplashActivity extends Activity implements LicenceManagerDelegate {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);

        // Animate logo
        animateLogo();

        // Register as event listener
        EventBus.getDefault().register(this);

        // Licence check
        if(getResources().getBoolean(R.bool.enable_licence_check)) {
            // Check for valid application licence (loadIssueCollection will be called afterwards
            BakerApplication.getInstance().getLicenceManager().setDelegate(this);
            BakerApplication.getInstance().getLicenceManager().checkAccess();
        }else{
            loadIssueCollection();
        }

        // Plugin Callback
        BakerApplication.getInstance().getPluginManager().onSplashActivityCreated(this);

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
        final IssueCollection issueCollection = BakerApplication.getInstance().getIssueCollection();
        issueCollection.load();
    }

    @Override
    protected void onDestroy() {
        // Register as event listener
        EventBus.getDefault().unregister(this);
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
        Log.i("LICENSING", "Invalid licence: " + reason);
        new AlertDialog.Builder(SplashActivity.this)
                .setTitle(getString(R.string.msg_exit))
                .setMessage(getString(R.string.err_application_license_problem))
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
                .setTitle(getString(R.string.msg_exit))
                .setMessage(getString(R.string.err_application_no_internet))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SplashActivity.this.finish();
                    }
                })
                .show();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(IssueCollectionLoadedEvent event) {

        // Unregister event listener
        EventBus.getDefault().unregister(this);

        // Launch shelf activity
        Intent i = new Intent(SplashActivity.this, ShelfActivity.class);
        startActivity(i);

        // Close Splash
        SplashActivity.this.finish();

    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(IssueCollectionErrorEvent event) {
        // No internet connection and no cached file
        new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.msg_exit))
                .setMessage(this.getString(R.string.err_application_no_internet))
                .setPositiveButton(this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SplashActivity.this.finish();
                    }
                })
                .show();
    }

}

package com.bakerframework.baker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.model.IssueCollection;
import com.bakerframework.baker.model.IssueCollectionListener;

/* @TODO: Provide some meaning to the loading screen */

public class SplashActivity extends Activity implements IssueCollectionListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);

        // Prepare issue collection
        IssueCollection issueCollection = BakerApplication.getInstance().getIssueCollection();
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

        // Launch shelf activity
        Intent i = new Intent(SplashActivity.this, ShelfActivity.class);
        startActivity(i);
        finish();
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

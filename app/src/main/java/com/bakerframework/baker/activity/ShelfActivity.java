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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.adapter.IssueAdapter;
import com.bakerframework.baker.events.DownloadIssueErrorEvent;
import com.bakerframework.baker.events.IssueCollectionLoadedEvent;
import com.bakerframework.baker.events.ParseBookJsonCompleteEvent;
import com.bakerframework.baker.events.ParseBookJsonErrorEvent;
import com.bakerframework.baker.model.*;
import com.bakerframework.baker.model.RemoteIssueCollection;
import com.bakerframework.baker.settings.Configuration;
import com.bakerframework.baker.settings.SettingsActivity;
import com.bakerframework.baker.view.IssueCardView;
import com.bakerframework.baker.view.ShelfView;
import com.path.android.jobqueue.JobManager;

import org.json.JSONException;
import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.ResponseCodes;
import org.solovyev.android.checkout.Sku;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ShelfActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final int STANDALONE_MAGAZINE_ACTIVITY_FINISH = 1;
    static final int SHELF_CHECKOUT_REQUEST_CODE = 0XCAFE;

    // Issues
    private ShelfView shelfView;
    private IssueAdapter issueAdapter;
    private IssueCollection issueCollection;

    // Features
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    // Billing
    private ActivityCheckout shelfCheckout;

    public ActivityCheckout getShelfCheckout() {
        return shelfCheckout;
    }

    // Jobs
    JobManager jobManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Initialize tutorial
        if(Configuration.getPrefFirstTimeRun()) {
            Log.d(this.getClass().getName(), "First time app running, launching tutorial.");
            showAppUsage();
        }

        // Initialize jobs
        jobManager = BakerApplication.getInstance().getJobManager();
        EventBus.getDefault().register(this);

        // Initialize issue collection
        issueCollection = BakerApplication.getInstance().getIssueCollection();

        // Initialize issue adapter for shelf view
        issueAdapter = new IssueAdapter(this, issueCollection);

        // Render View
        this.setContentView(R.layout.shelf_activity);

        // Initialize Features
        setupHeader();
        loadBackground();
        setupSwipeLayout();
        setupActionBar();
        setupCategoryDrawer();

        // Fade in animation
        View view = findViewById(android.R.id.content);
        Animation mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        mLoadAnimation.setDuration(2000);
        view.startAnimation(mLoadAnimation);

        // Initialize shelf view
        shelfView = (ShelfView) findViewById(R.id.shelf_view);
        shelfView.setAdapter(issueAdapter);
        issueAdapter.updateIssues();

        // Update category drawer
        updateCategoryDrawer(issueCollection.getCategories(), issueAdapter.getCategoryIndex());

        // Continue downloads
        unzipPendingPackages();

        // Checkout
        if(!Configuration.isStandaloneMode()) {
            shelfCheckout = Checkout.forActivity(this, BakerApplication.getInstance().getCheckout());
            shelfCheckout.start();
            shelfCheckout.createPurchaseFlow(SHELF_CHECKOUT_REQUEST_CODE, new PurchaseListener());
        }

        // Plugin Callback
        BakerApplication.getInstance().getPluginManager().onShelfActivityCreated(this);
    }

    public void openConnectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_no_connection_title))
                .setMessage(getString(R.string.msg_no_connection_message))
                .setPositiveButton(getString(R.string.msg_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ShelfActivity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton(getString(R.string.msg_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        swipeRefreshLayout.setRefreshing(false);
                        dialog.cancel();
                    }
                }).show();
    }

    private class PurchaseListener extends BaseRequestListener<Purchase> {

        @Override
        public void onSuccess(@NonNull Purchase purchase) {
            onPurchased();
        }

        private void onPurchased() {
            // let's update purchase information in local inventory
            Toast.makeText(getApplicationContext(), getString(R.string.msg_purchase_complete), Toast.LENGTH_SHORT).show();
            ShelfActivity.this.onRefresh();
        }

        @Override
        public void onError(int response, @NonNull Exception e) {
            // it is possible that our data is not synchronized with data on Google Play => need to handle some errors
            if (response == ResponseCodes.ITEM_ALREADY_OWNED) {
                onPurchased();
            } else {
                super.onError(response, e);
            }
        }
    }

    private abstract class BaseRequestListener<Request> implements RequestListener<Request> {

        @Override
        public void onError(int response, @NonNull Exception e) {
            // @TODO: add alert dialog or logging
            Log.e("ShelfActivity", e.getMessage());
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected void setupSwipeLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shelf, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Show / Hide subscription menu
        if(getResources().getStringArray(R.array.google_play_subscription_ids).length > 0) {
            menu.getItem(0).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();

        if (itemId == R.id.action_info) {
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra(IssueActivity.MODAL_URL, getString(R.string.asset_url_info));
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (itemId == R.id.action_refresh) {
            swipeRefreshLayout.setRefreshing(true);
            this.onRefresh();
            return true;
        } else if (itemId == R.id.action_subscribe) {
            if (BakerApplication.getInstance().isNetworkConnected()) {
                final List<Sku> subscriptionSkus = ((RemoteIssueCollection) issueCollection).getSubscriptionSkus();
                if(subscriptionSkus == null || subscriptionSkus.size() == 0) {
                    return false;
                }else if(subscriptionSkus.size() > 1) {
                    final String[] subscriptionItems = new String[subscriptionSkus.size()];
                    for(int i = 0; i < subscriptionSkus.size(); i++) {
                        subscriptionItems[i] = subscriptionSkus.get(i).title;
                    }
                    new AlertDialog.Builder(this)
                            .setTitle("Choose a subscription")
                            .setItems(subscriptionItems, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, final int which) {
                                    shelfCheckout.whenReady(new Checkout.ListenerAdapter() {
                                        @Override
                                        public void onReady(@NonNull BillingRequests requests) {
                                            onSubscriptionClicked(requests, subscriptionSkus.get(which));
                                        }
                                    });
                                }
                            }).show();
                }else{
                    shelfCheckout.whenReady(new Checkout.ListenerAdapter() {
                        @Override
                        public void onReady(@NonNull BillingRequests requests) {
                            onSubscriptionClicked(requests, subscriptionSkus.get(0));
                        }
                    });
                }
            }else {
                this.openConnectionDialog();
            }
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void onSubscriptionClicked(BillingRequests requests, Sku subscriptionSku) {
        requests.purchase(subscriptionSku, Configuration.getUserId(), shelfCheckout.getPurchaseFlow());
        BakerApplication.getInstance().getPluginManager().onSubscribeClicked(subscriptionSku);
    }

    public void presentIssues() {
        // Update Shelf Spinner
        swipeRefreshLayout.setRefreshing(false);

        // Update category drawer
        updateCategoryDrawer(issueCollection.getCategories(), issueAdapter.getCategoryIndex());

        // Update shelf view adapter
        issueAdapter.updateIssues();
    }

    private void loadBackground() {
        WebView webview = (WebView) findViewById(R.id.backgroundWebView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setBackgroundColor(Color.WHITE);
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webview.loadUrl(getString(R.string.asset_url_background));
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupCategoryDrawer() {

        // Create layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Create list
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Set up drawer toggle
        Button drawerToggle = (Button) findViewById(R.id.category_toggle);
        drawerToggle.setOnClickListener(new CategoryToggleClickListener());
    }

    private void updateCategoryDrawer(List<String> categories, int position) {
        if(categories == null || categories.size() == 0) {
            findViewById(R.id.category_toggle).setVisibility(View.GONE);
        }else{
            drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, categories));
            drawerList.setItemChecked(position, true);
        }
    }

    private void setupHeader() {
        WebView webview = (WebView) findViewById(R.id.headerView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        webview.setBackgroundColor(Color.TRANSPARENT);
        webview.setWebChromeClient(new WebChromeClient());
        webview.loadUrl(getString(R.string.asset_url_header));
    }

    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.shelf_actionbar);
    }

    public void viewIssue(final BookJson book) {
        Intent intent = new Intent(ShelfActivity.this, IssueActivity.class);
        try {
            intent.putExtra(Configuration.BOOK_JSON_KEY, book.toJSON().toString());
            intent.putExtra(Configuration.ISSUE_NAME, book.getMagazineName());
            startActivityForResult(intent, STANDALONE_MAGAZINE_ACTIVITY_FINISH);
        } catch (JSONException e) {
            Toast.makeText(this, "The book.json is invalid.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void showAppUsage() {
        BookJson book = new BookJson();
        book.setIssueName(this.getString(R.string.path_tutorial_directory));
        List<String> contents = new ArrayList<>();
        String[] pages = this.getResources().getStringArray(R.array.list_tutorial_pages);
        for (String page : pages) {
            page = page.trim();
            contents.add(page);
        }
        book.setContents(contents);
        book.setOrientation("portrait");
        Intent intent = new Intent(this, IssueActivity.class);

        try {
            intent.putExtra(Configuration.BOOK_JSON_KEY, book.toJSON().toString());
            intent.putExtra(Configuration.ISSUE_NAME, book.getMagazineName());
            intent.putExtra(Configuration.ISSUE_RETURN_TO_SHELF, true);
            intent.putExtra(Configuration.ISSUE_ENABLE_DOUBLE_TAP, false);
            intent.putExtra(Configuration.ISSUE_ENABLE_BACK_NEXT_BUTTONS, true);
            intent.putExtra(Configuration.ISSUE_ENABLE_TUTORIAL, true);
            startActivityForResult(intent, STANDALONE_MAGAZINE_ACTIVITY_FINISH);
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "Error parsing book json", e);
        }

    }

    private void unzipPendingPackages() {
        if(shelfView != null) {
            for (int i = 0; i < shelfView.getChildCount(); i++) {
                IssueCardView issueCardView = (IssueCardView) shelfView.getChildAt(i);
                if(issueCardView.getIssue().isDownloaded() && !issueCardView.getIssue().isDownloading() && !issueCardView.getIssue().isExtracted() && !issueCardView.getIssue().isExtracting()) {
                    // Continue issue extraction
                    Log.d(this.getClass().toString(), "Continue extract of " + issueCardView.getIssue().getName());
                    issueCardView.extractZip();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        final List<Issue> downloadingIssues = issueCollection.getDownloadingIssues();
        if (downloadingIssues.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                .setTitle(this.getString(R.string.msg_exit))
                .setMessage(this.getString(R.string.msg_closing_app))
                .setPositiveButton(this.getString(R.string.msg_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        issueCollection.cancelDownloadingIssues(downloadingIssues);
                        ShelfActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(this.getString(R.string.msg_no), null)
                .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(this.getClass().getName(), "MagazineActivity finished, resultCode: " + resultCode);
        if(requestCode == SHELF_CHECKOUT_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                onRefresh();
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.err_purchase_not_possible), Toast.LENGTH_SHORT).show();
                onRefresh();
            }
        }else if (resultCode == STANDALONE_MAGAZINE_ACTIVITY_FINISH) {
            this.finish();
        }
    }

    @Override
    public void onRefresh() {
        if (BakerApplication.getInstance().isNetworkConnected()) {
            issueCollection.load();
        }else {
            this.openConnectionDialog();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Categories

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
            onCategorySelected(index);
        }
    }

    /* The click listener for the category toggle */
    private class CategoryToggleClickListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View view) {
            if(drawerLayout.isDrawerOpen(Gravity.START)) {
                drawerLayout.closeDrawer(Gravity.START);
            }else{
                drawerLayout.openDrawer(Gravity.START);
            }
        }
    }

    private void onCategorySelected(int index) {
        String category = issueCollection.getCategories().get(index);
        issueAdapter.setCategory(category);

        // Update category drawer UI
        drawerList.setItemChecked(index, true);
        ((Button) findViewById(R.id.category_toggle)).setText(category);

        // Close category drawer
        drawerLayout.closeDrawer(drawerList);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DownloadIssueErrorEvent event) {
        // Restore purchases
        this.onRefresh();
        Toast.makeText(getApplicationContext(), getString(R.string.err_download_task_io), Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ParseBookJsonCompleteEvent event) {
        // View magazine
        viewIssue(event.getBookJson());
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ParseBookJsonErrorEvent event) {
        Toast.makeText(this, "The book.json was not found for issue " + event.getIssue().getName(), Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(IssueCollectionLoadedEvent event) {
        presentIssues();
    }

}
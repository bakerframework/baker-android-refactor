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
package com.bakerframework.baker.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.activity.ShelfActivity;
import com.bakerframework.baker.events.ArchiveIssueCompleteEvent;
import com.bakerframework.baker.events.DownloadIssueCompleteEvent;
import com.bakerframework.baker.events.DownloadIssueErrorEvent;
import com.bakerframework.baker.events.DownloadIssueProgressEvent;
import com.bakerframework.baker.events.ExtractIssueCompleteEvent;
import com.bakerframework.baker.events.ExtractIssueErrorEvent;
import com.bakerframework.baker.events.ExtractIssueProgressEvent;
import com.bakerframework.baker.events.IssueDataUpdatedEvent;
import com.bakerframework.baker.helper.ImageLoaderHelper;
import com.bakerframework.baker.jobs.ArchiveIssueJob;
import com.bakerframework.baker.jobs.ParseBookJsonJob;
import com.bakerframework.baker.model.Issue;
import com.bakerframework.baker.R;
import com.bakerframework.baker.settings.Configuration;

import org.solovyev.android.checkout.*;

import de.greenrobot.event.EventBus;

public class IssueCardView extends LinearLayout {

    private Issue issue;

    private boolean readable = false;

    private final int UI_STATE_INITIAL = 0;
    private final int UI_STATE_DOWNLOAD = 1;
    private final int UI_STATE_EXTRACT = 2;
    private final int UI_STATE_READY = 3;
    private final int UI_STATE_ARCHIVE = 4;
    private final int UI_STATE_ERROR = 5;

    // Layout elements
    LinearLayout uiIdleActionsContainer;
    LinearLayout uiPurchaseActionsContainer;
    LinearLayout uiReadyActionsContainer;
    LinearLayout uiProgressBarContainer;
    ImageView uiCoverImage;
    ProgressBar uiProgressBar;
    TextView uiProgressText;
    TextView uiTitleText;
    TextView uiInfoText;
    TextView uiDateText;
    TextView uiSizeText;
    Button uiBuyIssueButton;
    Button uiReadIssueButton;
    Button uiArchiveIssueButton;
    Button uiDownloadIssueButton;

    private Activity parentActivity;

    /**
     * Creates a reference to the parent / shelf activity
     *
     * @param parentActivity the parent Activity.
     */
    public IssueCardView(Context parentActivity, Issue issue) {
        super(parentActivity);

        this.parentActivity = (Activity)parentActivity;
        this.issue = issue;

    }

    public IssueCardView(Context context) {
        super(context);
    }


    /**
     * Initialize the view
     */
    public void init(final Context context) {

        // Prepare and inflate layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.issue_card_view, this, true);

        // Register UI elements
        uiIdleActionsContainer = (LinearLayout) findViewById(R.id.idle_actions_container);
        uiPurchaseActionsContainer = (LinearLayout) findViewById(R.id.purchase_actions_container);
        uiReadyActionsContainer = (LinearLayout) findViewById(R.id.ready_actions_container);
        uiProgressBarContainer = (LinearLayout) findViewById(R.id.progress_bar_container);
        uiProgressText = (TextView) findViewById(R.id.progress_text);
        uiCoverImage = (ImageView) findViewById(R.id.cover_image);
        uiProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        uiTitleText = (TextView) findViewById(R.id.title_text);
        uiInfoText = (TextView) findViewById(R.id.info_text);
        uiDateText = (TextView) findViewById(R.id.date_text);
        uiSizeText = (TextView) findViewById(R.id.size_text);
        uiBuyIssueButton = (Button) findViewById(R.id.buy_issue_button);
        uiReadIssueButton = (Button) findViewById(R.id.read_issue_button);
        uiArchiveIssueButton = (Button) findViewById(R.id.archive_issue_button);
        uiDownloadIssueButton = (Button) findViewById(R.id.download_issue_button);

        // Download cover
        ImageLoaderHelper.getImageLoader(context).displayImage(issue.getCover(), uiCoverImage);

        // Initialize cover click handler
        uiCoverImage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            if (readable && !issue.isDownloading()) {
                readIssue();
            }
            }
        });

        // Initialize purchase button click handler
        uiBuyIssueButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { purchaseIssue(); }
        });

        // Initialize download button click handler
        uiDownloadIssueButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { downloadIssue(); }
        });

        // Initialize read button click handler
        uiReadIssueButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                readIssue();
            }
        });

        // Initialize archive button click handler
        uiArchiveIssueButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                archiveIssue();
            }
        });

        // Initialize event listener
        EventBus.getDefault().register(this);

        // Redraw UI
        redraw();

    }

    public void redraw() {
        uiTitleText.setText(issue.getTitle());
        uiInfoText.setText(issue.getInfo());
        uiDateText.setText(issue.getDate());
        if(issue.hasPrice()) {
            uiBuyIssueButton.setText(issue.getPrice());
        }
        if (issue.getSize() == 0) {
            uiSizeText.setVisibility(View.GONE);
        } else {
            uiSizeText.setText(issue.getSizeMB() + " MB");
        }

        // Prepare actions
        if (issue.isExtracted()) {
            setUIState(UI_STATE_READY);
            readable = true;
        }else if(issue.getExtractJob() != null && !issue.getExtractJob().isCompleted()) {
            setUIState(UI_STATE_EXTRACT);
            readable = false;
        }else if(issue.isDownloading()){
            setUIState(UI_STATE_DOWNLOAD);
            readable = false;
        }else{
            setUIState(UI_STATE_INITIAL);
            readable = false;
        }

        requestLayout();
    }

    public Issue getIssue() {
        return issue;
    }

    private void purchaseIssue() {
        if (!issue.isPurchased()) {
            final ActivityCheckout checkout = ((ShelfActivity) parentActivity).getShelfCheckout();
            checkout.whenReady(new Checkout.ListenerAdapter() {
                @Override
                public void onReady(@NonNull BillingRequests requests) {
                    if(issue.getSku() != null) {
                        requests.purchase(issue.getSku(), Configuration.getUserId(), checkout.getPurchaseFlow());
                        BakerApplication.getInstance().getPluginManager().onIssuePurchaseClicked(issue);
                    }
                }
            });
        }
    }

    private void downloadIssue() {
        if (BakerApplication.getInstance().isNetworkConnected()) {
            setUIState(UI_STATE_DOWNLOAD);
            issue.startDownloadIssueJob();
            BakerApplication.getInstance().getPluginManager().onIssueDownloadClicked(issue);
        }else {
            ((ShelfActivity) this.parentActivity).openConnectionDialog();
        }
    }

    /**
     * Deletes and issue from the user device.
     */
    private void archiveIssue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.msg_confirmation)
                .setMessage(R.string.msg_confirmation_delete_text)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton(R.string.msg_no, null)
                .setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Update UI
                        setUIState(UI_STATE_ARCHIVE);
                        // Create and trigger archive task
                        ArchiveIssueJob archiveIssueJob = new ArchiveIssueJob(issue);
                        BakerApplication.getInstance().getJobManager().addJobInBackground(archiveIssueJob);
                        BakerApplication.getInstance().getPluginManager().onIssueArchiveClicked(issue);
                    }
                }).show();
    }

    /**
     * Change the views to start reading the issue.
     */
    private void readIssue() {
        ParseBookJsonJob parseBookJsonJob = new ParseBookJsonJob(issue);
        BakerApplication.getInstance().getJobManager().addJobInBackground(parseBookJsonJob);
        BakerApplication.getInstance().getPluginManager().onIssueReadClicked(issue);
    }

    /**
     * Start the unzipping task for an issue. Also handles the controls update.
     */
    public void extractZip() {
        setUIState(UI_STATE_EXTRACT);
        issue.startExtractIssueJob();
    }

    private void setUIState(int uiState) {
        switch (uiState) {
            case UI_STATE_INITIAL:
                uiReadyActionsContainer.setVisibility(View.GONE);
                if(issue.hasPrice() && !issue.isPurchased()) {
                    uiPurchaseActionsContainer.setVisibility(View.VISIBLE);
                    uiIdleActionsContainer.setVisibility(View.GONE);
                }else{
                    uiPurchaseActionsContainer.setVisibility(View.GONE);
                    uiIdleActionsContainer.setVisibility(View.VISIBLE);
                }
                uiProgressText.setVisibility(View.GONE);
                uiProgressBarContainer.setVisibility(View.GONE);
                uiProgressText.setText(null);
                break;
            case UI_STATE_DOWNLOAD:
                uiIdleActionsContainer.setVisibility(View.GONE);
                uiReadyActionsContainer.setVisibility(View.GONE);
                uiPurchaseActionsContainer.setVisibility(View.GONE);
                uiProgressText.setVisibility(View.VISIBLE);
                uiProgressBarContainer.setVisibility(View.VISIBLE);
                uiProgressText.setText(R.string.msg_issue_downloading);
                break;
            case UI_STATE_EXTRACT:
                uiIdleActionsContainer.setVisibility(View.GONE);
                uiReadyActionsContainer.setVisibility(View.GONE);
                uiPurchaseActionsContainer.setVisibility(View.GONE);
                uiProgressText.setVisibility(View.VISIBLE);
                uiProgressBarContainer.setVisibility(View.VISIBLE);
                uiProgressText.setText(R.string.msg_issue_extracting);
                break;
            case UI_STATE_READY:
                uiIdleActionsContainer.setVisibility(View.GONE);
                uiReadyActionsContainer.setVisibility(View.VISIBLE);
                uiPurchaseActionsContainer.setVisibility(View.GONE);
                uiProgressText.setVisibility(View.GONE);
                uiProgressBarContainer.setVisibility(View.GONE);
                uiProgressText.setText(null);
                break;
            case UI_STATE_ARCHIVE:
                uiIdleActionsContainer.setVisibility(View.GONE);
                uiReadyActionsContainer.setVisibility(View.GONE);
                uiProgressText.setVisibility(View.VISIBLE);
                uiProgressBarContainer.setVisibility(View.VISIBLE);
                uiProgressText.setText(R.string.msg_issue_deleting);
                break;
            case UI_STATE_ERROR:
                uiIdleActionsContainer.setVisibility(View.VISIBLE);
                uiReadyActionsContainer.setVisibility(View.GONE);
                uiPurchaseActionsContainer.setVisibility(View.GONE);
                uiProgressText.setVisibility(View.VISIBLE);
                uiProgressBarContainer.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ExtractIssueCompleteEvent event) {
        if(event.getIssue() == issue) {
            setUIState(UI_STATE_READY);
            readable = true;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ExtractIssueErrorEvent event) {
        if(event.getIssue() == issue) {
            setUIState(UI_STATE_INITIAL);
            readable = false;
            Toast.makeText(getContext(), "Could not extract this issue.", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ExtractIssueProgressEvent event) {
        if(event.getIssue() == issue) {
            uiProgressText.setText(parentActivity.getString(R.string.msg_issue_extracting) + ": " + String.valueOf(event.getProgress()) + "%");
            uiProgressBar.setProgress(event.getProgress());
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DownloadIssueCompleteEvent event) {
        if(event.getIssue() == issue) {
            // Trigger unzipping
            extractZip();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DownloadIssueErrorEvent event) {
        if(event.getIssue() == issue) {
            uiProgressText.setText(getContext().getString(R.string.err_download_task_io));
            setUIState(UI_STATE_ERROR);
            Toast.makeText(this.parentActivity, getContext().getString(R.string.err_download_task_io), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DownloadIssueProgressEvent event) {
        if(event.getIssue() == issue) {
            uiProgressText.setText(parentActivity.getString(R.string.msg_issue_downloading) + ": " + event.getProgress() + "% (" + String.valueOf(event.getBytesSoFar() / 1048576) + " MB)");
            uiProgressBar.setProgress(event.getProgress());
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(IssueDataUpdatedEvent event) {
        if(event.getIssue() == issue) {
            redraw();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ArchiveIssueCompleteEvent event) {
        if(event.getIssue() == issue) {
            readable = false;
            setUIState(UI_STATE_INITIAL);
        }
    }

}


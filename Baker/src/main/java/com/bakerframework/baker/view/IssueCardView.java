/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the Baker Framework nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
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
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.activity.ShelfActivity;
import com.bakerframework.baker.client.TaskMandator;
import com.bakerframework.baker.helper.ImageLoaderHelper;
import com.bakerframework.baker.model.Issue;
import com.bakerframework.baker.R;
import com.bakerframework.baker.model.BookJson;
import com.bakerframework.baker.task.ArchiveTask;
import com.bakerframework.baker.task.BookJsonParserTask;
import com.bakerframework.baker.task.UnzipperTask;

import org.json.JSONException;
import org.solovyev.android.checkout.*;

import java.text.ParseException;
import java.util.Observable;
import java.util.Observer;

public class IssueCardView extends LinearLayout implements TaskMandator, Observer {

    private Issue issue;

    private UnzipperTask unzipperTask;
    private boolean readable = false;

    private final int UNZIP_MAGAZINE_TASK = 2;
    private final int MAGAZINE_DELETE_TASK = 3;
    private final int BOOK_JSON_PARSE_TASK = 5;

    private final int UI_STATE_INITIAL = 0;
    private final int UI_STATE_DOWNLOAD = 1;
    private final int UI_STATE_UNZIP = 2;
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

        // Observe issue changes
        issue.addObserver(this);
    }

    public IssueCardView(Context context) {
        super(context);
    }

    /**
     * Initialize the view
     */
    public void init(final Context context, AttributeSet attrs) {

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

        // Download cover (if not exist)
        ImageLoaderHelper.getImageLoader(context).displayImage(issue.getCover(), uiCoverImage);

        // Initialize cover click handler
        uiCoverImage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (readable && !issue.isDownloading()) {
                    readIssue();
                }
            }
        });

        // Initialize download button click handler
        uiBuyIssueButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { purchaseIssue(); }
        });

        // Initialize download button click handler
        uiDownloadIssueButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setUIState(UI_STATE_DOWNLOAD);
                issue.startDownloadTask();
            }
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


        // Redraw UI
        redraw();

    }

    @Override
    protected void onDetachedFromWindow() {

        // Clean up


        super.onDetachedFromWindow();
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
        uiProgressText.setText("0 MB / " + issue.getSizeMB() + " MB");

        // Prepare actions
        if (issue.isExtracted()) {
            setUIState(UI_STATE_READY);
            readable = true;
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

    public void setBookJson(BookJson book) {

        if (book != null) {
            ShelfActivity activity = (ShelfActivity) getContext();
            activity.viewMagazine(book);
        } else {
            Toast.makeText(getContext(), "The book.json was not found!",
                    Toast.LENGTH_LONG).show();
        }

        // Here we register the OPEN ISSUE event on Google Analytics
        if (parentActivity.getResources().getBoolean(R.bool.ga_enable) && parentActivity.getResources().getBoolean(R.bool.ga_register_issue_read_event)) {
            ((BakerApplication) parentActivity.getApplication()).sendEvent(
                    parentActivity.getString(R.string.issues_category),
                    parentActivity.getString(R.string.issue_open),
                    issue.getName());
        }
    }

    private void purchaseIssue() {
        if (!issue.isPurchased()) {
            final ActivityCheckout checkout = ((ShelfActivity) parentActivity).getCheckout();
            checkout.whenReady(new Checkout.ListenerAdapter() {
                @Override
                public void onReady(@NonNull BillingRequests requests) {
                    if(issue.getSku() != null) {
                        requests.purchase(issue.getSku(), null, checkout.getPurchaseFlow());
                    }
                }
            });
        }
    }

    /**
     * Deletes and issue from the user device.
     */
    private void archiveIssue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirmation)
                .setMessage(R.string.archiveConfirm)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Update UI
                        setUIState(UI_STATE_ARCHIVE);
                        // Create and trigger archive task
                        // @TODO: Convert to listener logic
                        new ArchiveTask(IssueCardView.this, MAGAZINE_DELETE_TASK).execute(issue.getName());
                    }
                }).show();
    }

    /**
     * Change the views to start reading the issue.
     */
    private void readIssue() {
        BookJsonParserTask parser = new BookJsonParserTask(getContext(), issue, this, BOOK_JSON_PARSE_TASK);
        if (issue.isStandalone()) {
            parser.execute("STANDALONE");
        } else {
            parser.execute(issue.getName());
        }
    }

    /**
     * Start the unzipping task for an issue. Also handles the controls update.
     */
    public void startUnzip() {

        // Update UI
        setUIState(UI_STATE_UNZIP);

        // Create and trigger unzipper task
        unzipperTask = new UnzipperTask(parentActivity, this, UNZIP_MAGAZINE_TASK);
        unzipperTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, issue.getHpubPath(), issue.getName());
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
                uiProgressText.setText(R.string.downloading);
                break;
            case UI_STATE_UNZIP:
                uiIdleActionsContainer.setVisibility(View.GONE);
                uiReadyActionsContainer.setVisibility(View.GONE);
                uiPurchaseActionsContainer.setVisibility(View.GONE);
                uiProgressText.setVisibility(View.VISIBLE);
                uiProgressBarContainer.setVisibility(View.VISIBLE);
                uiProgressText.setText(R.string.unzipping);
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
                uiProgressText.setText(R.string.deleting);
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

    // @TODO: this should be gone soon
    @Override
    public void updateProgress(int taskId, Long... progress) {

    }

    /**
     * As an instance of GindMandator, this class should implement a method postExecute
     * to be executed when one of the tasks is done.
     *
     * @param taskId  the id of the task that ended.
     * @param results the results values.
     */
    @Override
    public void postExecute(final int taskId, String... results) {

        //TODO: Handle failures.
        switch (taskId) {
            case UNZIP_MAGAZINE_TASK:
                //If the Unzip tasks ended successfully we will update the UI to let the user
                //start reading the issue.
                unzipperTask = null;
                if (results[0].equals("SUCCESS")) {
                    setUIState(UI_STATE_READY);
                    readable = true;
                } else {
                    setUIState(UI_STATE_INITIAL);
                    readable = false;
                    Toast.makeText(getContext(), "Could not extract the package. Possibly corrupted.", Toast.LENGTH_LONG).show();
                }
                break;
            case MAGAZINE_DELETE_TASK:
                //If the issue files were deleted successfully the UI will be updated to let
                //the user download the issue again.
                if (results[0].equals("SUCCESS")) {
                    // Here we register the DELETE ISSUE event on Google Analytics
                    if (parentActivity.getResources().getBoolean(R.bool.ga_enable) && parentActivity.getResources().getBoolean(R.bool.ga_register_issue_delete_event)) {
                        ((BakerApplication)parentActivity.getApplication()).sendEvent(
                                parentActivity.getString(R.string.issues_category),
                                parentActivity.getString(R.string.issue_delete),
                                issue.getName());
                    }

                    readable = false;
                    setUIState(UI_STATE_INITIAL);
                }
                break;
            case BOOK_JSON_PARSE_TASK:
                try {
                    BookJson bookJson = new BookJson();

                    final String magazineName = results[0];
                    final String rawJson = results[1];

                    bookJson.setMagazineName(magazineName);
                    bookJson.fromJson(rawJson);

                    setBookJson(bookJson);
                } catch (JSONException | ParseException ex) {
                    Log.e(getClass().getName(), "Error parsing the book.json", ex);
                }

                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    public void update(Observable observable, Object o) {
        if(observable == issue) {
            switch ((int) o) {
                case Issue.EVENT_ON_DOWNLOAD_PROGRESS:
                    uiProgressText.setText(String.valueOf(issue.getBytesSoFar() / 1048576) + " MB (" + (int) issue.getProgress()+ " %)");
                    uiProgressBar.setProgress((int) issue.getProgress());
                    break;
                case Issue.EVENT_ON_DOWNLOAD_COMPLETE:
                    // Send google analytics event
                    if (parentActivity.getResources().getBoolean(R.bool.ga_enable) && parentActivity.getResources().getBoolean(R.bool.ga_register_issue_download_event)) {
                        BakerApplication.getInstance().sendEvent(
                                parentActivity.getString(R.string.issues_category),
                                parentActivity.getString(R.string.issue_download),
                                issue.getName());
                    }
                    // Trigger unzipping
                    startUnzip();
                    break;
                case Issue.EVENT_ON_DOWNLOAD_FAILED:
                    setUIState(UI_STATE_ERROR);
                    uiProgressText.setText(getContext().getString(R.string.download_task_error_io));
                    break;
            }
        }
    }

}


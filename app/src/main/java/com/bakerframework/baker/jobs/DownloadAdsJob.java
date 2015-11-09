package com.bakerframework.baker.jobs;

import android.util.Log;

import com.admag.AdmagSDK;
import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.events.DownloadAdsCompleteEvent;
import com.bakerframework.baker.events.ExtractIssueProgressEvent;
import com.bakerframework.baker.model.BookJson;
import com.bakerframework.baker.model.Issue;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

/**
 * Created by rubio on 22/09/15.
 */
public class DownloadAdsJob extends Job {

    private final Issue issue;

    public DownloadAdsJob(Issue issue) {
        super(new Params(Priority.MID).setPersistent(false).setGroupId(issue.getName()));
        this.issue = issue;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        BookJson bookJson = new BookJson();
        //count total pages
        bookJson.fromIssue(issue);

        AdmagSDK.cacheAd(BakerApplication.getInstance().getBaseContext()
                , Integer.parseInt(BakerApplication.getInstance().getString(
                        R.string.admag_publication_id)),
                issue.getTitle(), bookJson.getContents().size(), issue.getTitle(),
                issue.getCover(), null);

        //recount total pages and add admag ads
        bookJson.fromIssue(issue);

        Log.i("DownloadAdsJob", "completed");
        EventBus.getDefault().post(new DownloadAdsCompleteEvent(issue));

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}

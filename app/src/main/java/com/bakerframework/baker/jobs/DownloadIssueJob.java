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
package com.bakerframework.baker.jobs;

import android.util.Log;

import com.bakerframework.baker.events.DownloadIssueCompleteEvent;
import com.bakerframework.baker.events.DownloadIssueErrorEvent;
import com.bakerframework.baker.events.DownloadIssueProgressEvent;
import com.bakerframework.baker.handler.DownloadHandler;
import com.bakerframework.baker.model.Issue;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

public class DownloadIssueJob extends Job {
    private final Issue issue;
    private boolean completed;
    private DownloadHandler downloadHandler;

    public DownloadIssueJob(Issue issue) {
        super(new Params(Priority.LOW).requireNetwork().setPersistent(false).setGroupId(issue.getName()));
        this.issue = issue;
        this.completed = false;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        // Download file
         downloadHandler = new DownloadHandler(issue.getUrl()) {
            @Override
            public void onDownloadProgress(int percentComplete, long bytesSoFar, long totalBytes) {
                EventBus.getDefault().post(new DownloadIssueProgressEvent(issue, percentComplete, bytesSoFar, totalBytes));
            }
        };
        downloadHandler.download(issue.getHpubFile());

        // Complete job
        completed = true;
        EventBus.getDefault().post(new DownloadIssueCompleteEvent(issue));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Log.e("DownloadIssueJob", throwable.getLocalizedMessage());
        completed = true;
        EventBus.getDefault().post(new DownloadIssueErrorEvent(issue, throwable));
        return false;
    }

    @Override
    protected void onCancel() {
        downloadHandler.cancel();
    }

    public Issue getIssue() {
        return issue;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void cancel() {
        this.completed = true;
    }

}

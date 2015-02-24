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

import com.bakerframework.baker.events.ArchiveIssueCompleteEvent;
import com.bakerframework.baker.model.Issue;
import com.bakerframework.baker.settings.Configuration;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.io.File;

import de.greenrobot.event.EventBus;

public class ArchiveIssueJob extends Job {
    private final Issue issue;
    private boolean completed;

    public ArchiveIssueJob(Issue issue) {
        super(new Params(Priority.MID).setPersistent(false).setGroupId(issue.getName()));
        this.issue = issue;
        completed = false;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        // Delete directory
        File issueDirectory = new File(Configuration.getMagazinesDirectory(), issue.getName());
        Configuration.deleteDirectory(issueDirectory.getAbsolutePath());

        // Post complete event
        completed = true;
        Log.i("ArchiveIssueJob", "completed");
        EventBus.getDefault().post(new ArchiveIssueCompleteEvent(issue));
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    public boolean isCompleted() {
        return completed;
    }

}

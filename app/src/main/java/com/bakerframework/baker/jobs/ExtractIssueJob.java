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

import android.support.annotation.NonNull;
import android.util.Log;

import com.bakerframework.baker.events.ExtractIssueCompleteEvent;
import com.bakerframework.baker.events.ExtractIssueProgressEvent;
import com.bakerframework.baker.model.Issue;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.greenrobot.event.EventBus;

public class ExtractIssueJob extends Job {
    private final Issue issue;
    private final File zipFile;
    private final File outputDirectory;
    private boolean completed;

    public ExtractIssueJob(Issue issue) {
        super(new Params(Priority.MID).setPersistent(false).setGroupId(issue.getName()));
        this.issue = issue;
        this.zipFile = issue.getHpubFile();
        this.outputDirectory = new File(this.zipFile.getParent(), issue.getName());
        completed = false;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        // Send zero progress event
        EventBus.getDefault().post(new ExtractIssueProgressEvent(issue, 0));

        // Delete directory if exists
        if (outputDirectory.exists() && outputDirectory.isDirectory()) {
            FileUtils.deleteDirectory(outputDirectory);
        }

        // Prepare progress
        FileInputStream zipFileInputStream = new MonitorFileInputStream(zipFile);

        // Unzip file
        ZipUtil.unpack(zipFileInputStream, outputDirectory);

        // Delete zip file
        if(!this.zipFile.delete()) {
            throw new Exception("Unable to remove issue hpub file");
        }

        // Post complete event
        completed = true;
        Log.i("ExtractZipJob", "completed");
        EventBus.getDefault().post(new ExtractIssueCompleteEvent(issue));
    }

    @Override
    protected void onCancel() {
        completed = true;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    public Issue getIssue() {
        return issue;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void cancel() {
        // @TODO: Cancel extract action
    }

    private class MonitorFileInputStream extends FileInputStream {
        private double totalRead;
        private double totalSize;
        private int percentComplete;

        public MonitorFileInputStream(File file) throws IOException {
            super(file);
            totalSize = this.available();
            totalRead = 0;
            percentComplete = 0;
        }

        @Override
        public int read(@NonNull byte[] buffer, int byteOffset, int byteCount) throws IOException {
            totalRead = totalRead + byteCount;
            int newPercentComplete = (int) Math.floor((totalRead / totalSize) * 100);
            if(newPercentComplete > percentComplete) {
                percentComplete = newPercentComplete;
                EventBus.getDefault().post(new ExtractIssueProgressEvent(issue, percentComplete));
            }
            return super.read(buffer, byteOffset, byteCount);
        }
    }

}

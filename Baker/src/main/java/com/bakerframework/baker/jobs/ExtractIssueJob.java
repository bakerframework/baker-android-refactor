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

import com.bakerframework.baker.events.ExtractIssueCompleteEvent;
import com.bakerframework.baker.events.ExtractIssueProgressEvent;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.greenrobot.event.EventBus;

public class ExtractIssueJob extends Job {
    private final String id;
    private File zipFile;
    private File outputDirectory;
    private boolean completed;

    public ExtractIssueJob(String id, String zipFilePath, String outputDirectoryName) {
        super(new Params(Priority.LOW).setPersistent(false));
        Log.i("ExtractZipJob", "JOB CREATED");
        this.id = id;
        this.zipFile = new File(zipFilePath);
        this.outputDirectory = new File(this.zipFile.getParent(), outputDirectoryName);
        completed = false;
    }

    @Override
    public void onAdded() {
        Log.i("ExtractZipJob", "JOB ADDED");
    }

    @Override
    public void onRun() throws Throwable {
        Log.i("ExtractZipJob", "start");

        // Send zero progress event
        EventBus.getDefault().post(new ExtractIssueProgressEvent(0, id));

        // Delete directory if exists
        if (outputDirectory.exists() && outputDirectory.isDirectory()) {
            FileUtils.deleteDirectory(outputDirectory);
        }

        // Prepare progress
        FileInputStream zipFileInputStream = new MonitorFileInputStream(zipFile);

        // Unzip file
        ZipUtil.unpack(zipFileInputStream, outputDirectory);

        // Delete zip file
        this.zipFile.delete();

        // Post complete event
        completed = true;
        Log.i("ExtractZipJob", "completed");
        EventBus.getDefault().post(new ExtractIssueCompleteEvent(id));
    }

    @Override
    protected void onCancel() {
        completed = true;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    public String getId() {
        return id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void cancel() {
        // @TODO: Implement unzip cancellation logic
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
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            totalRead = totalRead + byteCount;
            int newPercentComplete = (int) Math.floor((totalRead / totalSize) * 100);
            if(newPercentComplete > percentComplete) {
                percentComplete = newPercentComplete;
                EventBus.getDefault().post(new ExtractIssueProgressEvent(percentComplete, id));
            }
            return super.read(buffer, byteOffset, byteCount);
        }
    }

}

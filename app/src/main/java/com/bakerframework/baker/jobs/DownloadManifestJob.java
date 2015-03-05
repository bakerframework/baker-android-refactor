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

import com.bakerframework.baker.events.DownloadManifestCompleteEvent;
import com.bakerframework.baker.events.DownloadManifestErrorEvent;
import com.bakerframework.baker.handler.DownloadHandler;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.io.File;

import de.greenrobot.event.EventBus;

public class DownloadManifestJob extends Job {
    private final String url;
    private final File targetFile;
    private boolean completed;
    private DownloadHandler downloadHandler;

    public DownloadManifestJob(String url, File targetFile) {
        super(new Params(Priority.LOW).setPersistent(false).setGroupId("application"));
        this.url= url;
        this.targetFile = targetFile;
        this.completed = false;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Log.d("DownloadManifestJob", "DOWNLOADING FILE: " + url);

        // Download file
        downloadHandler = new DownloadHandler(this.url);
        downloadHandler.download(this.targetFile);

        // Complete job
        completed = true;
        EventBus.getDefault().post(new DownloadManifestCompleteEvent());
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Log.e("DownloadManifestJob", throwable.getLocalizedMessage());
        completed = true;
        EventBus.getDefault().post(new DownloadManifestErrorEvent(throwable));
        return false;
    }

    @Override
    protected void onCancel() {
        downloadHandler.cancel();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void cancel() {
        this.completed = true;
    }

}

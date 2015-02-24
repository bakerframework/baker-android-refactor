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

import com.bakerframework.baker.events.FetchPurchasesCompleteEvent;
import com.bakerframework.baker.events.FetchPurchasesErrorEvent;
import com.bakerframework.baker.handler.ApiRequestHandler;
import com.bakerframework.baker.handler.DownloadHandler;
import com.bakerframework.baker.settings.Configuration;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;

import de.greenrobot.event.EventBus;

public class FetchPurchasesJob extends Job {
    private final String url;
    private boolean completed;
    private DownloadHandler downloadHandler;

    public FetchPurchasesJob(String url) {
        super(new Params(Priority.LOW).requireNetwork().setPersistent(false).setGroupId("application"));
        this.url= url;
        this.completed = false;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Log.d("FetchPurchasesJob", "fetching purchases from " + url);
        String purchasesResult = "";

        // Build request handler
        ApiRequestHandler apiRequestHandler = new ApiRequestHandler(Configuration.getPurchasesUrl());

        // Post purchase verification request
        boolean success = apiRequestHandler.get();
        completed = true;

        if(success) {
            FetchPurchasesResponse fetchPurchasesResponse = apiRequestHandler.getResponseObject(FetchPurchasesResponse.class);
            EventBus.getDefault().post(new FetchPurchasesCompleteEvent(fetchPurchasesResponse));
        }else{
            EventBus.getDefault().post(new FetchPurchasesErrorEvent(new Exception(apiRequestHandler.getResponseText())));
        }

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Log.e("FetchPurchasesJob", throwable.getLocalizedMessage());
        completed = true;
        EventBus.getDefault().post(new FetchPurchasesErrorEvent(throwable));
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

    public class FetchPurchasesResponse {
        public List<String> issues;
        public boolean subscribed;
    }
}

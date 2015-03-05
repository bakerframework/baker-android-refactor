/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 * Copyright (c) 2015. Tobias Strebitzer, Francisco Contreras, Holland Salazar.
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * <p/>
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
 */
package com.bakerframework.baker.handler;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class ApiRequestHandler {
    private final String url;
    private final HttpClient httpclient;
    private HttpRequestBase request;
    private int statusCode;
    private String responseText;

    public ApiRequestHandler(String url) {
        this.url = url;
        httpclient = new DefaultHttpClient();
    }

    public boolean get() {
        request = new HttpGet(url);
        try {
            execute();
            return true;
        } catch (IOException e) {
            onError(new ApiResponseException(10000, e.getMessage()));
        } catch (ApiResponseException e) {
            onError(e);
        }
        return false;
    }

    public boolean post(Object params) {
        request = new HttpPost(url);
        try {
            Gson gson = new Gson();
            ((HttpPost) request).setEntity(new StringEntity(gson.toJson(params)));
            execute();
            return true;
        } catch (IOException e) {
            onError(new ApiResponseException(10000, e.getMessage()));
        } catch (ApiResponseException e) {
            onError(e);
        }
        return false;
    }

    private void execute() throws IOException, ApiResponseException {
        responseText = null;
        request.addHeader("content-type", "application/json;charset=UTF-8");
        HttpResponse response = httpclient.execute(request);
        statusCode = response.getStatusLine().getStatusCode();
        if(response.getEntity() != null) {
            responseText = EntityUtils.toString(response.getEntity(), "UTF-8");
        }
        if (statusCode < 200 || statusCode >= 300) {
            throw new ApiResponseException(statusCode, responseText);
        }
    }

    public void cancel() {
        if(request != null && !request.isAborted()) {
            request.abort();
            request = null;
        }
    }

    public void onError(Throwable throwable) {
        Log.e("ApiRequestHandler", throwable.getMessage());
    }

    public String getResponseText() {
        return (responseText == null) ? "" : responseText;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public <T> T getResponseObject(Class responseClass) {
        Gson gson = new Gson();
        return (T) gson.fromJson(getResponseText(), responseClass);
    }

    private class ApiResponseException extends Exception {
        private final int statusCode;
        public ApiResponseException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
        public int getStatusCode() {
            return statusCode;
        }
    }

}

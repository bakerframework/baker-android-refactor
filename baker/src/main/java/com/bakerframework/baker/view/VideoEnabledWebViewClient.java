package com.bakerframework.baker.view;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bakerframework.baker.R;
import com.bakerframework.baker.activity.IssueActivity;
import com.bakerframework.baker.settings.Configuration;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

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
public class VideoEnabledWebViewClient extends WebViewClient {

    private final WebViewFragment webViewFragment;
    private final IssueActivity issueActivity;

    public VideoEnabledWebViewClient(WebViewFragment webViewFragment) {
        this.webViewFragment = webViewFragment;
        this.issueActivity = ((IssueActivity) webViewFragment.getActivity());
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String stringUrl) {

        // mailto links will be handled by the OS.
        if (stringUrl.startsWith("mailto:")) {
            Uri uri = Uri.parse(stringUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            webViewFragment.startActivity(intent);
        } else {
            try {
                URL url = new URL(stringUrl);

                // We try to remove the referrer string to avoid passing it to the server in case the URL is an external link.
                String referrer = "";
                if (url.getQuery() != null) {
                    Map<String, String> variables = Configuration.splitUrlQueryString(url);
                    String finalQueryString = "";
                    for (Map.Entry<String, String> entry : variables.entrySet()) {
                        if (entry.getKey().equals("referrer")) {
                            referrer = entry.getValue();
                        } else {
                            finalQueryString += entry.getKey() + "=" + entry.getValue() + "&";
                        }
                    }
                    if (!finalQueryString.isEmpty()) {
                        finalQueryString = "?" + finalQueryString.substring(0, finalQueryString.length() - 1);
                    }
                    stringUrl = stringUrl.replace("?" + url.getQuery(), finalQueryString);
                }
                // Remove referrer from query string
                if (!url.getProtocol().equals("file")) {
                    if (referrer.equals(issueActivity.getString(R.string.url_external_referrer))) {
                        Uri uri = Uri.parse(stringUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        webViewFragment.startActivity(intent);
                    } else if (referrer.equals(issueActivity.getString(R.string.url_baker_referrer))) {
                        issueActivity.openLinkInModal(stringUrl);
                        return true;
                    } else {
                        // We return false to tell the webview that we are not going to handle the URL override.
                        return false;
                    }
                } else {
                    stringUrl = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
                    int index = issueActivity.getJsonBook().getContents().indexOf(stringUrl);
                    if (index != -1) {
                        Log.d(this.getClass().toString(), "Index to load: " + index + ", page: " + stringUrl);
                        issueActivity.getPager().setCurrentItem(index);
                        view.setVisibility(View.GONE);
                    } else {
                        // If the file DOES NOT exist, we won't load it.
                        File htmlFile = new File(url.getPath());
                        if (htmlFile.exists()) {
                            return false;
                        }
                    }
                }
            } catch (MalformedURLException | UnsupportedEncodingException ex) {
                Log.d(">>>URL_DATA", ex.getMessage());
            }
        }

        return true;
    }

}

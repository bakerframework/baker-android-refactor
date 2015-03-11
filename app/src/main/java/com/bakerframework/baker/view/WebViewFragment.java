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
package com.bakerframework.baker.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakerframework.baker.R;
import com.bakerframework.baker.activity.IssueActivity;
import com.bakerframework.baker.settings.Configuration;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class WebViewFragment extends Fragment {

    private XWalkView webView;
    private String baseUrl;
    private boolean isInitialized = false;
    private boolean isUserVisible = false;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// The last two arguments ensure LayoutParams are inflated properly.
        View rootView = inflater.inflate(R.layout.web_view_fragment, container, false);
        baseUrl = getArguments().getString("object");

        // Instantiate views
        webView = (XWalkView) rootView.findViewById(R.id.pageWebView);
        // webView.setBackgroundColor(Color.TRANSPARENT);

        if(!isInitialized) {
            isInitialized = true;
            initializeWebView();
        }

		return rootView;
	}

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isUserVisible = isVisibleToUser;
        if (isUserVisible && isInitialized && webView != null) {
            webView.resumeTimers();
        }else if(webView != null) {
            webView.pauseTimers();
        }
    }

    public void initializeWebView() {
        // Initialize the webview

        webView.setResourceClient(new XWalkResourceClient(webView) {
            @Override
            public boolean shouldOverrideUrlLoading(XWalkView view, String stringUrl) {

                if(stringUrl.equals(baseUrl)) {
                    return false;
                }

                // mailto links will be handled by the OS.
                if (stringUrl.startsWith("mailto:")) {
                    Uri uri = Uri.parse(stringUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    WebViewFragment.this.startActivity(intent);
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
                            if (referrer.equals(WebViewFragment.this.getActivity().getString(R.string.url_external_referrer))) {
                                Uri uri = Uri.parse(stringUrl);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                WebViewFragment.this.startActivity(intent);
                            } else if (referrer.toLowerCase().equals(WebViewFragment.this.getActivity().getString(R.string.url_baker_referrer))) {
                                ((IssueActivity) WebViewFragment.this.getActivity()).openLinkInModal(stringUrl);
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            stringUrl = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
                            int index = ((IssueActivity) WebViewFragment.this.getActivity()).getJsonBook().getContents().indexOf(stringUrl);
                            if (index != -1) {
                                Log.d(this.getClass().toString(), "Index to load: " + index + ", page: " + stringUrl);
                                ((IssueActivity) WebViewFragment.this.getActivity()).getViewPager().setCurrentItem(index);
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
        });

        // Set UI Client (Start stop animations)
        webView.setUIClient(new XWalkUIClient(webView) {

            @Override
            public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
                if(!url.isEmpty() && status == LoadStatus.FINISHED) {
                    if(isUserVisible) {
                        webView.resumeTimers();
                    }else{
                        webView.pauseTimers();
                    }

                }
            }
        });
        webView.load(baseUrl, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.onDestroy();
        }
    }


	
	public String getUrl() {
		return this.webView.getUrl();
	}

    public XWalkView getWebView() {
        return webView;
    }

}
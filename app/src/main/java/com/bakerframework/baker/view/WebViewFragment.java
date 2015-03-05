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
import android.view.WindowManager;
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

public class WebViewFragment extends Fragment {

    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// The last two arguments ensure LayoutParams are inflated properly.
		View rootView = inflater.inflate(R.layout.web_view_fragment, container, false);
		Bundle args = getArguments();

        // Initialize the webview
		webView = (VideoEnabledWebView) rootView.findViewById(R.id.pageWebView);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = rootView.findViewById(R.id.nonVideoLayout);
        ViewGroup videoLayout = (ViewGroup) rootView.findViewById(R.id.videoLayout);
        View loadingView = inflater.inflate(R.layout.view_loading_video, null, false);
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView);
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen) {
                    WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getActivity().getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }else{
                    WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getActivity().getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String stringUrl) {

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
                                ((IssueActivity) WebViewFragment.this.getActivity()).getPager().setCurrentItem(index);
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

        webView.loadUrl(args.getString("object"));

		return rootView;
	}

    @Override
    public void onDestroy() {
        this.getWebView().loadUrl("about:blank");
        this.getWebView().pauseTimers();
        this.getWebView().destroy();
        super.onDestroy();
    }
	
	public String getUrl() {
		return this.webView.getUrl();
	}

	public boolean inCustomView() {
        return (webChromeClient.isVideoFullscreen());
    }

    public void hideCustomView() {
    	webChromeClient.onHideCustomView();
    }

    public VideoEnabledWebView getWebView() {
        return webView;
    }

}
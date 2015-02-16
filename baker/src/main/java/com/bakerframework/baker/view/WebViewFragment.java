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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakerframework.baker.R;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends Fragment {

    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// The last two arguments ensure LayoutParams are inflated properly.
		View rootView = inflater.inflate(R.layout.webview_fragment, container, false);
		Bundle args = getArguments();

		webView = (VideoEnabledWebView) rootView.findViewById(R.id.pageWebView);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = rootView.findViewById(R.id.nonVideoLayout);
        ViewGroup videoLayout = (ViewGroup) rootView.findViewById(R.id.videoLayout);
        View loadingView = inflater.inflate(R.layout.view_loading_video, null);
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView);
        webView.setWebViewClient(new VideoEnabledWebViewClient(this));
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
        // @TODO: disable double-tap when in custom view
        // return (customView != null);
        return false;
    }

    public void hideCustomView() {
    	webChromeClient.onHideCustomView();
    }

    public VideoEnabledWebView getWebView() {
        return webView;
    }

}
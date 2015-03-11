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
package com.bakerframework.baker.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import com.bakerframework.baker.R;

import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

public class ModalActivity extends Activity {

    private int orientation;
    private XWalkView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.modal_activity);

        // Here we allow the user to rotate the screen.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        Intent intent = getIntent();
        String baseUrl = intent.getStringExtra(IssueActivity.MODAL_URL);
        orientation = intent.getIntExtra(IssueActivity.ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        // Click on the CLOSE button.
        findViewById(R.id.btnCloseModal).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ModalActivity.this.finish();
            }
        });

        // Initialize the webview
        webView = (XWalkView) findViewById(R.id.modalWebView);
        webView.setResourceClient(new XWalkResourceClient(webView) {
            @Override
            public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
                return false;
            }
        });

        webView.load(baseUrl, null);
    }

    @Override
    public void onStop() {
        this.setRequestedOrientation(orientation);
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.getNavigationHistory().canGoBack()){
            webView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onDestroy() {
        webView.pauseTimers();
        webView.onDestroy();
        super.onDestroy();
    }

}



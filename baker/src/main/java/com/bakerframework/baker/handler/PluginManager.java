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

import android.app.Activity;
import android.util.Log;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.model.Issue;
import com.bakerframework.baker.plugin.BakerPlugin;

import org.solovyev.android.checkout.Sku;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginManager {
    private final List<BakerPlugin> plugins;

    public PluginManager() {
        plugins = new ArrayList<>();
        List<String> pluginClassNames = Arrays.asList(BakerApplication.getInstance().getResources().getStringArray(R.array.baker_plugins));
        for (String pluginClassName : pluginClassNames) {
            try {
                Class pluginClass = Class.forName("com.bakerframework.baker.plugin." + pluginClassName);
                Class[] types = {};
                Constructor constructor = pluginClass.getConstructor(types);
                BakerPlugin plugin = (BakerPlugin) constructor.newInstance();
                plugins.add(plugin);
            } catch (Exception e) {
                Log.e("BakerApplication", "Plugin Error: " + e.getMessage());
            }
        }
        Log.d("BakerApplication", "Initialized " + plugins.size() + " plugins");
    }

    // Activity Events

    public void onSplashActivityCreated(Activity activity) {
        for(BakerPlugin plugin : plugins) {
            plugin.onSplashActivityCreated(activity);
        }
    }

    public void onShelfActivityCreated(Activity activity) {
        for(BakerPlugin plugin : plugins) {
            plugin.onShelfActivityCreated(activity);
        }
    }

    public void onIssueActivityCreated(Activity activity) {
        for(BakerPlugin plugin : plugins) {
            plugin.onIssueActivityCreated(activity);
        }
    }

    // Shelf / Issue Events

    public void onIssueDownloadClicked(Issue issue) {
        for(BakerPlugin plugin : plugins) {
            plugin.onIssueDownloadClicked(issue);
        }
    }

    public void onIssuePurchaseClicked(Issue issue) {
        for(BakerPlugin plugin : plugins) {
            plugin.onIssuePurchaseClicked(issue);
        }
    }

    public void onIssueArchiveClicked(Issue issue) {
        for(BakerPlugin plugin : plugins) {
            plugin.onIssueArchiveClicked(issue);
        }
    }

    // Issue Navigation Events

    public void onIssuePageOpened(Issue issue, String pageTitle, int pageIndex) {
        for(BakerPlugin plugin : plugins) {
            plugin.onIssuePageOpened(issue, pageTitle, pageIndex);
        }
    }

    // Purchase Events

    public void onIssueReadClicked(Issue issue) {
        for(BakerPlugin plugin : plugins) {
            plugin.onIssueReadClicked(issue);
        }
    }

    public void onSubscribeClicked(Sku subscription) {
        for(BakerPlugin plugin : plugins) {
            plugin.onSubscribeClicked(subscription);
        }
    }

}

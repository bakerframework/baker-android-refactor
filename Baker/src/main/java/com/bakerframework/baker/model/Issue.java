/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the Baker Framework nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
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
package com.bakerframework.baker.model;

import android.os.AsyncTask;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.settings.Configuration;
import com.bakerframework.baker.task.DownloadTask;
import com.bakerframework.baker.task.DownloadTaskDelegate;

import java.io.File;
import java.util.List;
import java.util.Observable;

public class Issue extends Observable implements DownloadTaskDelegate {

    // Private members
    private String name;
    private String productId;
    private String title;
    private String info;
    private String date;
    private List<String> categories;
    private Integer size;
    private String cover;
    private String url;
    private String price;
    private boolean standalone;
    private boolean coverChanged;
    private boolean urlChanged;
    private DownloadTask downloadTask;

    // Events
    public static final int EVENT_ON_DOWNLOAD_PROGRESS = 0;
    public static final int EVENT_ON_DOWNLOAD_COMPLETE = 1;
    public static final int EVENT_ON_DOWNLOAD_FAILED= 2;

    // Download variables
    private long bytesSoFar;
    private long progress;
    private long totalBytes;

    // Constructor

    public Issue(String name) {
        this.name = name;
    }

    // Task handling

    public void cancelDownloadTask() {
        if(downloadTask != null) {
            downloadTask.cancel(true);
        }
    }

    public void startDownloadTask() {
        // Create and trigger download task
        downloadTask = new DownloadTask(this, getUrl(), getHpubFile());
        downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    public boolean isDownloading() {
        return downloadTask != null && downloadTask.isDownloading();
    }

    // Getters & Setters

    public String getName() {
        return name;
    }

    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getCategories() {
        return categories;
    }
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }

    public String getCover() {
        return cover;
    }
    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price= price;
    }
    public boolean hasPrice() {
        return (this.price != null);
    }

    public boolean isCoverChanged() {
        return coverChanged;
    }
    public void setCoverChanged(boolean coverChanged) {
        this.coverChanged = coverChanged;
    }

    public boolean isUrlChanged() {
        return urlChanged;
    }
    public void setUrlChanged(boolean urlChanged) {
        this.urlChanged = urlChanged;
    }

    public boolean isStandalone() {
        return standalone;
    }
    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    public long getBytesSoFar() {
        return bytesSoFar;
    }
    public long getProgress() {
        return progress;
    }
    public long getTotalBytes() {
        return totalBytes;
    }

    // Helpers

    public Integer getSizeMB() {
        return size / 1048576;
    }

    public String getBookJsonPath() {
        return Configuration.getMagazinesDirectory() + File.separator + name + File.separator + BakerApplication.getInstance().getString(R.string.book);
    }

    public File getBookJsonFile() {
        return new File(getBookJsonPath());
    }

    public String getHpubPath() {
        return Configuration.getMagazinesDirectory() + File.separator + name + BakerApplication.getInstance().getString(R.string.package_extension);
    }

    public File getHpubFile() {
        return new File(getHpubPath());
    }

    public boolean isExtracted() {
        return getBookJsonFile().exists() && getBookJsonFile().isFile();
    }

    public boolean isDownloaded() {
        File archiveFile = getHpubFile();
        return  (downloadTask == null || !downloadTask.isDownloading()) && archiveFile.exists() && archiveFile.isFile();
    }

    public boolean isInCategory(String category) {
        return categories.contains(category);
    }

    // Delegates

    @Override
    public void onDownloadProgress(DownloadTask task, long progress, long bytesSoFar, long totalBytes) {
        if(task == downloadTask) {
            this.progress = progress;
            this.bytesSoFar = bytesSoFar;
            this.totalBytes = totalBytes;
            setChanged();
            notifyObservers(EVENT_ON_DOWNLOAD_PROGRESS);
        }
    }

    @Override
    public void onDownloadComplete(DownloadTask task, File file) {
        setChanged();
        notifyObservers(EVENT_ON_DOWNLOAD_COMPLETE);
    }

    @Override
    public void onDownloadFailed(DownloadTask task) {
        setChanged();
        notifyObservers(EVENT_ON_DOWNLOAD_FAILED);
    }


}

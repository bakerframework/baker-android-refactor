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
package com.bakerframework.baker.model;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.events.IssueDataUpdatedEvent;
import com.bakerframework.baker.helper.FileHelper;
import com.bakerframework.baker.jobs.DownloadIssueJob;
import com.bakerframework.baker.jobs.ExtractIssueJob;
import com.bakerframework.baker.settings.Configuration;

import org.json.JSONObject;
import org.solovyev.android.checkout.Sku;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

public class Issue {
    private final String name;
    private String productId;
    private String title;
    private String info;
    private String date;
    private Date objDate;
    private List<String> categories;
    private Integer size;
    private String cover;
    private String url;
    private Sku sku;
    private boolean purchased;
    private boolean standalone;
    private boolean coverChanged;
    private boolean urlChanged;
    private DownloadIssueJob downloadJob;
    private ExtractIssueJob extractJob;

    public Issue(String name) {
        this.name = name;
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

    public Date getObjDate() {
        return objDate;
    }
    public void setObjDate(Date objDate) {
        this.objDate= objDate;
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
        return (sku != null) ? sku.price : null;
    }
    public boolean hasPrice() {
        return (sku != null);
    }

    public Sku getSku() {
        return sku;
    }
    public void setSku(Sku sku) {
        boolean needsUpdate = (sku != this.sku);
        this.sku = sku;
        if(needsUpdate) { sendUpdateEvent(); }
    }
    public boolean hasSku() {
        return (this.sku != null);
    }

    public void setPurchased(boolean purchased) {
        boolean needsUpdate = (purchased != this.purchased);
        this.purchased = purchased;
        if(needsUpdate) { sendUpdateEvent(); }
    }
    public boolean isPurchased() {
        return this.purchased;
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

    // Tasks and Jobs

    public void sendUpdateEvent() {
        EventBus.getDefault().post(new IssueDataUpdatedEvent(this));
    }

    public DownloadIssueJob getDownloadJob() {
        return downloadJob;
    }

    public void cancelDownloadJob() {
        if(downloadJob != null) { downloadJob.cancel(); }
    }

    public boolean isDownloading() {
        return downloadJob != null && !downloadJob.isCompleted();
    }

    public boolean isDownloaded() {
        File archiveFile = getHpubFile();
        return  (downloadJob == null || downloadJob.isCompleted()) && archiveFile.exists() && archiveFile.isFile();
    }

    public void startDownloadIssueJob() {
        downloadJob = new DownloadIssueJob(this);
        BakerApplication.getInstance().getJobManager().addJobInBackground(downloadJob);
    }

    public ExtractIssueJob getExtractJob() {
        return extractJob;
    }

    public void cancelExtractJob() {
        if(extractJob != null) { extractJob.cancel(); }
    }

    public boolean isExtracting() {
        return getExtractJob() != null && !getExtractJob().isCompleted();
    }

    public boolean isExtracted() {
        return !isExtracting() && !getHpubFile().exists() && isBookJsonFilePresent();
    }

    public void startExtractIssueJob() {
        extractJob = new ExtractIssueJob(this);
        BakerApplication.getInstance().getJobManager().addJobInBackground(extractJob);
    }

    // File Management

    public String getBookJsonPath() {
        return Configuration.getMagazinesDirectory() + File.separator + name + File.separator + BakerApplication.getInstance().getString(R.string.path_book);
    }

    public File getBookJsonFile() {
        return new File(getBookJsonPath());
    }

    public boolean isBookJsonFilePresent() {
        if(isStandalone()) {
            try {
                return Arrays.asList(BakerApplication.getInstance().getAssets().list("books/" + name)).contains("book.json");
            } catch (IOException e) {
                return false;
            }
        }else{
            return getBookJsonFile().exists() && getBookJsonFile().isFile();
        }
    }

    public String getHpubPath() {
        return Configuration.getMagazinesDirectory() + File.separator + name + BakerApplication.getInstance().getString(R.string.path_package_extension);
    }

    public File getHpubFile() {
        return new File(getHpubPath());
    }

    // Helpers

    public Integer getSizeMB() {
        return size / 1048576;
    }

    public boolean isInCategory(String category) {
        return categories.contains(category);
    }

    public JSONObject getBookJsonObject() {
        if(isStandalone()) {
            return FileHelper.getJsonObjectFromAsset("books" + File.separator + name + File.separator + "book.json");
        }else{
            File bookJsonDirectory = new File(Configuration.getMagazinesDirectory(), getName());
            File bookJsonFile = new File(bookJsonDirectory, BakerApplication.getInstance().getString(R.string.path_book));
            return FileHelper.getJsonObjectFromFile(bookJsonFile);
        }

    }

}

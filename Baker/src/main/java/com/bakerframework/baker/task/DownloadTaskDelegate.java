package com.bakerframework.baker.task;

import java.io.File;

/**
 * Created by tstrebitzer on 15/12/14.
 */
public interface DownloadTaskDelegate {
    void onDownloadProgress(DownloadTask task, long progress, long bytesSoFar, long totalBytes);
    void onDownloadComplete(DownloadTask task, File file);
    void onDownloadFailed(DownloadTask task);
}

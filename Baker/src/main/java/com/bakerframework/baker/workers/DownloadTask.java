package com.bakerframework.baker.workers;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.bakerframework.baker.client.TaskMandator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Long, String> {

    // Task management
    private TaskMandator mandator;
    private int taskId;

    // Input arguments
    private String downloadUrl;
    private String fileName;
    private String downloadDirectoryPath;

    // Download handling
    Uri downloadedFile;
    private boolean downloading;
    private HttpURLConnection connection;

    // Helper variables
    private String tag;

    public DownloadTask(TaskMandator mandator, final int taskId, final String downloadUrl, final String fileName, final String downloadDirectoryPath) {

        // Task management
        this.mandator = mandator;
        this.taskId = taskId;
        this.downloading = false;

        // Replace URL
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.downloadDirectoryPath = downloadDirectoryPath;

        // Helper variables
        this.tag = this.getClass().toString();
    }

    // Public methods

    public boolean isDownloading() {
        return downloading;
    }

    // Main routine

    @Override
    protected String doInBackground(String... params) {
        Log.d(tag, "DOWNLOADING FILE: " + downloadUrl);
        String result = "";
        downloading = true;
        try {
            // Prepare download
            File file = this.createTargetFile();
            // Download file
            downloadedFile = this.downloadFile(file);
            if(downloadedFile == null) {
                result = "ERROR";
            }else{
                result = "SUCCESS";
            }
        } catch (Exception e) {
            Log.w(tag, "Error while retrieving file from " + downloadUrl, e);
            result = "ERROR";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            downloading = false;
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        mandator.updateProgress(taskId, progress[0], progress[1], progress[2]);
    }

    @Override
    protected void onPostExecute(String result) {
        String path = (null == downloadedFile) ? "" : downloadedFile.getPath();
        mandator.postExecute(taskId, result, path);
    }

    // Helper methods

    private String getFilePath() {
        return downloadDirectoryPath.concat(File.separator).concat(fileName);
    }

    private File createTargetFile() throws IOException {
        File directory = new File(downloadDirectoryPath);
        File file = new File(this.getFilePath());
        if (!directory.exists()) { directory .mkdirs(); }
        if(!file.exists()) { file.createNewFile(); }
        return file;
    }

    private Uri downloadFile(File file) throws IOException {

        // Prepare download
        connection = null;
        URL url = new URL(downloadUrl);
        connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(true);

        // Prepare streams
        long totalBytes = connection.getContentLength();
        InputStream inputStream = connection.getInputStream();
        OutputStream output = new FileOutputStream( file );

        // Prepare iterators
        long bytesSoFar = 0;
        long progress = 0;
        byte[] buffer = new byte[4096];
        int bytesRead = - 1;

        // Download stream
        while ( (bytesRead = inputStream.read(buffer)) != -1) {
            // Check if the task was cancelled
            if (isCancelled()) break;
            // Check for read bytes
            if (bytesRead > 0) {
                // Update progress
                if (totalBytes != -1) {
                    bytesSoFar = bytesSoFar + bytesRead;
                    progress = (long)((float)bytesSoFar/totalBytes*100);
                    publishProgress(progress, bytesSoFar, totalBytes);
                    Log.i(tag, "Percentage complete: " + progress + "%");
                }
                // Write to file
                output.write(buffer, 0, bytesRead);
            }
        }
        output.close();

        // Return file uri
        if (isCancelled()) {
            Log.i(tag, "Task was cancelled at " + progress + "%");
            return null;
        }else{
            return Uri.parse("file://".concat(this.getFilePath()));
        }

    }

}

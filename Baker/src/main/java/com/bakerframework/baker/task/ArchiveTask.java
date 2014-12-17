package com.bakerframework.baker.task;

import android.os.AsyncTask;

import com.bakerframework.baker.client.TaskMandator;
import com.bakerframework.baker.settings.Configuration;

import java.io.File;

public class ArchiveTask extends AsyncTask<String, Long, String> {

    private TaskMandator mandator;

    private int taskId;

    public ArchiveTask(TaskMandator mandator, final int taskId) {
        this.mandator = mandator;
        this.taskId = taskId;
    }

    @Override
    protected String doInBackground(String... params) {
        String filePath = Configuration.getMagazinesDirectory() + File.separator + params[0];
        return Configuration.deleteDirectory(filePath) ? "SUCCESS" : "ERROR";
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
    }

    @Override
    protected void onPostExecute(final String result) {
        mandator.postExecute(taskId, result);
    }

}

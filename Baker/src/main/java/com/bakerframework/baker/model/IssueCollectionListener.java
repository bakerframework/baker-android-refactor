package com.bakerframework.baker.model;

import java.util.List;

/**
 * Created by tstrebitzer on 15/12/14.
 */
public interface IssueCollectionListener {
    void onIssueCollectionLoaded();
    void onIssueCollectionLoadError();
}

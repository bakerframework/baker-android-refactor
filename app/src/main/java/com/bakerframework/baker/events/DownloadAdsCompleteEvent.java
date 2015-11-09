

    package com.bakerframework.baker.events;

    import com.bakerframework.baker.model.Issue;

    public class DownloadAdsCompleteEvent {
        private final Issue issue;

        public DownloadAdsCompleteEvent(Issue issue) {
            this.issue = issue;
        }

        public Issue getIssue() {
            return issue;
        }
    }


package minibrowser.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryItem {
    private int id;
    private String title;
    private String url;
    private String visitedAt;

    public HistoryItem() {
    }

    public HistoryItem(int id, String title, String url, String visitedAt) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.visitedAt = visitedAt;
    }

    public HistoryItem(String title, String url) {
        this.title = title;
        this.url = url;
        this.visitedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVisitedAt() {
        return visitedAt;
    }

    public void setVisitedAt(String visitedAt) {
        this.visitedAt = visitedAt;
    }

    @Override
    public String toString() {
        return (title != null && !title.isEmpty() ? title : url) + " (" + visitedAt + ")";
    }
}

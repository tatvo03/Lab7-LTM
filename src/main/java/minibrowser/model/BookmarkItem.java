package minibrowser.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookmarkItem {
    private int id;
    private String title;
    private String url;
    private String createdAt;

    public BookmarkItem() {
    }

    public BookmarkItem(int id, String title, String url, String createdAt) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.createdAt = createdAt;
    }

    public BookmarkItem(String title, String url) {
        this.title = title;
        this.url = url;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title != null && !title.isEmpty() ? title : url;
    }
}

package minibrowser.dao;

import minibrowser.database.Database;
import minibrowser.model.BookmarkItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookmarkDAO {

    public List<BookmarkItem> getAllBookmarks() {
        List<BookmarkItem> list = new ArrayList<>();
        String sql = "SELECT id, title, url, created_at FROM bookmarks ORDER BY id ASC";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BookmarkItem item = new BookmarkItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("url"),
                        rs.getString("created_at")
                );
                list.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách bookmark: " + e.getMessage());
        }
        return list;
    }

    public boolean addBookmark(String title, String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "INSERT INTO bookmarks (title, url, created_at) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE title = VALUES(title), created_at = VALUES(created_at)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, (title == null || title.trim().isEmpty()) ? url : title.trim());
            pstmt.setString(2, url.trim());
            pstmt.setString(3, now);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm bookmark: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBookmark(int id) {
        String sql = "DELETE FROM bookmarks WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa bookmark theo ID: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBookmarkByUrl(String url) {
        if (url == null) return false;
        String sql = "DELETE FROM bookmarks WHERE url = ? OR url = ? OR url = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String trimmed = url.trim();
            pstmt.setString(1, trimmed);
            pstmt.setString(2, trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed + "/");
            pstmt.setString(3, trimmed);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa bookmark theo URL: " + e.getMessage());
            return false;
        }
    }

    public boolean isBookmarked(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT id FROM bookmarks WHERE url = ? OR url = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String trimmed = url.trim();
            pstmt.setString(1, trimmed);
            pstmt.setString(2, trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed + "/");
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra bookmark: " + e.getMessage());
            return false;
        }
    }

    public List<BookmarkItem> searchBookmarks(String keyword) {
        List<BookmarkItem> list = new ArrayList<>();
        String sql = "SELECT id, title, url, created_at FROM bookmarks WHERE title LIKE ? OR url LIKE ? ORDER BY id ASC";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookmarkItem(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("url"),
                            rs.getString("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm bookmark: " + e.getMessage());
        }
        return list;
    }
}

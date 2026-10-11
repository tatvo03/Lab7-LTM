package minibrowser.dao;

import minibrowser.database.Database;
import minibrowser.model.HistoryItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {

    public boolean addHistory(String title, String url) {
        if (url == null || url.trim().isEmpty() || url.startsWith("data:") || url.equals("about:blank")) {
            return false;
        }
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "INSERT INTO history (title, url, visited_at) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, (title == null || title.trim().isEmpty()) ? url : title.trim());
            pstmt.setString(2, url.trim());
            pstmt.setString(3, now);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi lưu lịch sử: " + e.getMessage());
            return false;
        }
    }

    public List<HistoryItem> getAllHistory() {
        List<HistoryItem> list = new ArrayList<>();
        String sql = "SELECT id, title, url, visited_at FROM history ORDER BY id DESC LIMIT 500";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new HistoryItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("url"),
                        rs.getString("visited_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy lịch sử: " + e.getMessage());
        }
        return list;
    }

    public List<HistoryItem> searchHistory(String keyword) {
        List<HistoryItem> list = new ArrayList<>();
        String sql = "SELECT id, title, url, visited_at FROM history WHERE title LIKE ? OR url LIKE ? ORDER BY id DESC LIMIT 200";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new HistoryItem(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("url"),
                            rs.getString("visited_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm lịch sử: " + e.getMessage());
        }
        return list;
    }

    public boolean deleteHistory(int id) {
        String sql = "DELETE FROM history WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa mục lịch sử: " + e.getMessage());
            return false;
        }
    }

    public boolean clearHistory() {
        String sql = "DELETE FROM history";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa toàn bộ lịch sử: " + e.getMessage());
            return false;
        }
    }
}

package minibrowser.dao;

import minibrowser.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDAO {

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT `value` FROM settings WHERE `key` = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đọc cài đặt: " + e.getMessage());
        }
        return defaultValue;
    }

    public boolean setSetting(String key, String value) {
        String sql = "INSERT INTO settings (`key`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi lưu cài đặt: " + e.getMessage());
            return false;
        }
    }

    public String getHomepage() {
        return getSetting("homepage", "https://www.google.com");
    }

    public void setHomepage(String homepage) {
        setSetting("homepage", homepage);
    }

    public String getSearchEngine() {
        return getSetting("search_engine", "https://www.bing.com/search?q=");
    }

    public void setSearchEngine(String searchEngine) {
        setSetting("search_engine", searchEngine);
    }

    public boolean isShowBookmarksBar() {
        return Boolean.parseBoolean(getSetting("show_bookmarks_bar", "true"));
    }

    public void setShowBookmarksBar(boolean show) {
        setSetting("show_bookmarks_bar", String.valueOf(show));
    }

    public String getTheme() {
        return getSetting("theme", "light");
    }

    public void setTheme(String theme) {
        setSetting("theme", theme);
    }
}

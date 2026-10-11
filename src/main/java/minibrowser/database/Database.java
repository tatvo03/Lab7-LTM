package minibrowser.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Database {
    private static String dbHost = "localhost";
    private static int dbPort = 3306;
    private static String dbName = "minibrowser";
    private static String dbUser = "root";
    private static String dbPassword = "";

    private static boolean connected = false;
    private static String lastError = null;

    static {
        loadConfig();
        try {
            // Nạp MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            initDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL JDBC Driver: " + e.getMessage());
            lastError = "Thiếu thư viện com.mysql.cj.jdbc.Driver";
        }
    }

    private static void loadConfig() {
        try (InputStream is = Database.class.getResourceAsStream("/db.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                dbHost = props.getProperty("db.host", dbHost).trim();
                dbPort = Integer.parseInt(props.getProperty("db.port", String.valueOf(dbPort)).trim());
                dbName = props.getProperty("db.name", dbName).trim();
                dbUser = props.getProperty("db.user", dbUser).trim();
                dbPassword = props.getProperty("db.password", dbPassword).trim();
            }
        } catch (Exception e) {
            System.err.println("Không đọc được tệp cấu hình db.properties, dùng cấu hình mặc định: " + e.getMessage());
        }
    }

    public static String getServerUrl() {
        return "jdbc:mysql://" + dbHost + ":" + dbPort + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    }

    public static String getDbUrl() {
        return "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), dbUser, dbPassword);
    }

    public static boolean isConnected() {
        return connected;
    }

    public static String getLastError() {
        return lastError;
    }

    public static synchronized void initDatabase() {
        loadConfig();

        // 1. Kết nối vào MySQL Server để đảm bảo CSDL tồn tại
        try (Connection serverConn = DriverManager.getConnection(getServerUrl(), dbUser, dbPassword);
             Statement stmt = serverConn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        } catch (SQLException e) {
            connected = false;
            lastError = e.getMessage();
            System.err.println("====================================================================");
            System.err.println("[CẢNH BÁO KẾT NỐI CSDL]");
            System.err.println("Không thể kết nối đến máy chủ MySQL tại " + dbHost + ":" + dbPort + "!");
            System.err.println("-> Vui lòng mở XAMPP Control Panel và bấm nút [Start] ở mục MySQL.");
            System.err.println("Chi tiết lỗi: " + e.getMessage());
            System.err.println("====================================================================");
            return;
        }

        // 2. Khởi tạo các bảng trong CSDL
        String createBookmarksSql = """
                CREATE TABLE IF NOT EXISTS bookmarks (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(255) NOT NULL,
                    url VARCHAR(500) NOT NULL UNIQUE,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        String createHistorySql = """
                CREATE TABLE IF NOT EXISTS history (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(255),
                    url VARCHAR(1000) NOT NULL,
                    visited_at DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        String createSettingsSql = """
                CREATE TABLE IF NOT EXISTS settings (
                    `key` VARCHAR(100) PRIMARY KEY,
                    `value` TEXT NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createBookmarksSql);
            stmt.execute(createHistorySql);
            stmt.execute(createSettingsSql);

            // Khởi tạo các bookmark mặc định nếu bảng trống
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bookmarks")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertDefaultBookmark(conn, "Google", "https://www.google.com");
                    insertDefaultBookmark(conn, "YouTube", "https://www.youtube.com");
                    insertDefaultBookmark(conn, "GitHub", "https://github.com");
                    insertDefaultBookmark(conn, "Wikipedia", "https://vi.wikipedia.org");
                    insertDefaultBookmark(conn, "VnExpress", "https://vnexpress.net");
                }
            }

            // Khởi tạo các cài đặt mặc định nếu chưa có
            insertDefaultSetting(conn, "homepage", "https://www.google.com");
            insertDefaultSetting(conn, "search_engine", "https://www.bing.com/search?q=");
            insertDefaultSetting(conn, "show_bookmarks_bar", "true");
            insertDefaultSetting(conn, "theme", "light");

            connected = true;
            lastError = null;
            System.out.println("[Database] Kết nối CSDL MySQL (" + dbName + ") thành công!");

        } catch (SQLException e) {
            connected = false;
            lastError = e.getMessage();
            System.err.println("Lỗi khởi tạo bảng MySQL: " + e.getMessage());
        }
    }

    private static void insertDefaultBookmark(Connection conn, String title, String url) {
        String sql = "INSERT IGNORE INTO bookmarks (title, url) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, url);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi thêm bookmark mặc định: " + e.getMessage());
        }
    }

    private static void insertDefaultSetting(Connection conn, String key, String defaultValue) {
        String sql = "INSERT IGNORE INTO settings (`key`, `value`) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, defaultValue);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi thêm cài đặt mặc định: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== KIỂM TRA KẾT NỐI CSDL MYSQL ===");
        initDatabase();
        if (isConnected()) {
            System.out.println("=> Kết nối MySQL THÀNH CÔNG! Đã tạo CSDL và bảng đầy đủ.");
        } else {
            System.err.println("=> Kết nối THẤT BẠI: " + getLastError());
            System.err.println("Hãy mở XAMPP Control Panel và nhấn [Start] ở module MySQL.");
        }
    }
}

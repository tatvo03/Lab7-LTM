package minibrowser.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import minibrowser.dao.BookmarkDAO;
import minibrowser.dao.SettingsDAO;
import minibrowser.database.Database;
import minibrowser.model.BookmarkItem;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

    static {
        // Tắt toàn bộ các cảnh báo định dạng media không tương thích từ WebKit nội bộ của JavaFX
        minibrowser.Launcher.silenceWebKitMediaWarnings();
    }

    private Stage primaryStage;
    private TabPane tabPane;
    private HBox bookmarksBar;
    private StackPane rootPane;
    private VBox toastContainer;

    private SettingsDAO settingsDAO;
    private BookmarkDAO bookmarkDAO;

    private BookmarkView bookmarkView;
    private HistoryView historyView;

    private boolean isDarkMode = false;
    private String homepageUrl = "https://www.google.com";
    private String searchEngineUrl = "https://www.bing.com/search?q=";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Bật quản lý Cookie cho toàn bộ phiên duyệt web
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        // Khởi tạo database và DAO
        Database.initDatabase();
        this.settingsDAO = new SettingsDAO();
        this.bookmarkDAO = new BookmarkDAO();

        this.bookmarkView = new BookmarkView(this);
        this.historyView = new HistoryView(this);

        // Đọc cài đặt
        loadSettings();

        // Xây dựng giao diện Chrome
        buildUI();

        // Mở tab đầu tiên
        createNewTab(null);

        // Thiết lập Scene & Shortcuts
        Scene scene = new Scene(rootPane, 1100, 750);
        applyTheme(scene);
        setupKeyboardShortcuts(scene);

        primaryStage.setTitle("MiniBrowser - Trình duyệt Web");
        primaryStage.setScene(scene);
        primaryStage.show();

        if (!Database.isConnected()) {
            Platform.runLater(() -> {
                showNotification("Cảnh báo CSDL", "Chưa kết nối MySQL (port 3306). Vui lòng mở XAMPP và bấm Start MySQL!");
            });
        }
    }

    private void loadSettings() {
        this.homepageUrl = settingsDAO.getHomepage();
        this.searchEngineUrl = settingsDAO.getSearchEngine();
        this.isDarkMode = "dark".equalsIgnoreCase(settingsDAO.getTheme());
    }

    private void buildUI() {
        rootPane = new StackPane();

        VBox mainContainer = new VBox();
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        // Top Chrome Bar: TabPane + '+' Button + 3-dots Menu
        tabPane = new TabPane();
        tabPane.getStyleClass().add("chrome-tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        HBox.setHgrow(tabPane, Priority.ALWAYS);

        Button btnNewTab = new Button("+");
        btnNewTab.getStyleClass().add("btn-new-tab");
        btnNewTab.setTooltip(new Tooltip("Tab mới (Ctrl+T)"));
        btnNewTab.setOnAction(e -> createNewTab(null));

        MenuButton btnMenu = createChromeMenu();

        HBox topControls = new HBox(6);
        topControls.setAlignment(Pos.CENTER_RIGHT);
        topControls.setPadding(new Insets(6, 10, 0, 0));
        topControls.getChildren().addAll(btnNewTab, btnMenu);

        HBox tabStrip = new HBox();
        tabStrip.setAlignment(Pos.CENTER_LEFT);
        tabStrip.getChildren().addAll(tabPane, topControls);

        // Bookmarks Bar (Quick Access bar)
        bookmarksBar = new HBox(6);
        bookmarksBar.setAlignment(Pos.CENTER_LEFT);
        bookmarksBar.getStyleClass().add("bookmarks-bar");
        boolean showBookmarks = settingsDAO.isShowBookmarksBar();
        bookmarksBar.setVisible(showBookmarks);
        bookmarksBar.setManaged(showBookmarks);
        refreshBookmarksBar();

        mainContainer.getChildren().addAll(tabStrip, bookmarksBar);

        // Toast container (floating notification at bottom right)
        toastContainer = new VBox();
        toastContainer.setAlignment(Pos.BOTTOM_RIGHT);
        toastContainer.setPadding(new Insets(0, 20, 20, 0));
        toastContainer.setMouseTransparent(true);

        rootPane.getChildren().addAll(mainContainer, toastContainer);
    }

    private MenuButton createChromeMenu() {
        MenuButton menuBtn = new MenuButton("⋮");
        menuBtn.getStyleClass().add("chrome-menu-button");
        menuBtn.setTooltip(new Tooltip("Tùy chỉnh và điều khiển MiniBrowser"));

        MenuItem itemNewTab = new MenuItem("🗂️  Tab mới           Ctrl+T");
        itemNewTab.setOnAction(e -> createNewTab(null));

        MenuItem itemNewWindow = new MenuItem("🪟  Cửa sổ mới       Ctrl+N");
        itemNewWindow.setOnAction(e -> openNewWindow());

        SeparatorMenuItem sep1 = new SeparatorMenuItem();

        MenuItem itemBookmarks = new MenuItem("⭐  Quản lý Dấu trang  Ctrl+Shift+O");
        itemBookmarks.setOnAction(e -> bookmarkView.show());

        MenuItem itemToggleBookmarksBar = new MenuItem("📌  Hiện/Ẩn thanh dấu trang  Ctrl+Shift+B");
        itemToggleBookmarksBar.setOnAction(e -> toggleBookmarksBar());

        MenuItem itemHistory = new MenuItem("📜  Lịch sử duyệt web  Ctrl+H");
        itemHistory.setOnAction(e -> historyView.show());

        SeparatorMenuItem sep2 = new SeparatorMenuItem();

        MenuItem itemZoomIn = new MenuItem("🔍  Phóng to          Ctrl +");
        itemZoomIn.setOnAction(e -> {
            BrowserTab activeTab = getActiveTab();
            if (activeTab != null) activeTab.zoomIn();
        });

        MenuItem itemZoomOut = new MenuItem("🔍  Thu nhỏ          Ctrl -");
        itemZoomOut.setOnAction(e -> {
            BrowserTab activeTab = getActiveTab();
            if (activeTab != null) activeTab.zoomOut();
        });

        MenuItem itemZoomReset = new MenuItem("🔍  Thu phóng 100%    Ctrl 0");
        itemZoomReset.setOnAction(e -> {
            BrowserTab activeTab = getActiveTab();
            if (activeTab != null) activeTab.resetZoom();
        });

        SeparatorMenuItem sep3 = new SeparatorMenuItem();

        MenuItem itemToggleTheme = new MenuItem(isDarkMode ? "☀️  Chuyển sang Giao diện Sáng" : "🌙  Chuyển sang Giao diện Tối");
        itemToggleTheme.setOnAction(e -> {
            toggleTheme();
            itemToggleTheme.setText(isDarkMode ? "☀️  Chuyển sang Giao diện Sáng" : "🌙  Chuyển sang Giao diện Tối");
        });

        MenuItem itemSettings = new MenuItem("⚙️  Cài đặt");
        itemSettings.setOnAction(e -> showSettingsDialog());

        MenuItem itemAbout = new MenuItem("ℹ️  Giới thiệu về MiniBrowser");
        itemAbout.setOnAction(e -> showAboutDialog());

        menuBtn.getItems().addAll(
                itemNewTab, itemNewWindow, sep1,
                itemBookmarks, itemToggleBookmarksBar, itemHistory, sep2,
                itemZoomIn, itemZoomOut, itemZoomReset, sep3,
                itemToggleTheme, itemSettings, itemAbout
        );

        return menuBtn;
    }

    public void refreshBookmarksBar() {
        if (bookmarksBar == null) return;
        bookmarksBar.getChildren().clear();

        List<BookmarkItem> bookmarks = bookmarkDAO.getAllBookmarks();
        for (BookmarkItem item : bookmarks) {
            Button btnChip = new Button("🌐 " + item.getTitle());
            btnChip.getStyleClass().add("bookmark-chip");
            btnChip.setTooltip(new Tooltip(item.getTitle() + "\n" + item.getUrl()));

            btnChip.setOnAction(e -> {
                BrowserTab current = getActiveTab();
                if (current != null) {
                    current.loadUrl(item.getUrl());
                } else {
                    createNewTab(item.getUrl());
                }
            });

            // Context menu for bookmark chip
            ContextMenu contextMenu = new ContextMenu();
            MenuItem openInNewTab = new MenuItem("Mở trong Tab mới");
            openInNewTab.setOnAction(ev -> createNewTab(item.getUrl()));

            MenuItem deleteBookmark = new MenuItem("Xóa dấu trang này");
            deleteBookmark.setOnAction(ev -> {
                bookmarkDAO.deleteBookmark(item.getId());
                refreshBookmarksBar();
                BrowserTab active = getActiveTab();
                if (active != null) active.updateStarButton(active.getCurrentUrl());
                showNotification("Dấu trang", "Đã xóa: " + item.getTitle());
            });

            contextMenu.getItems().addAll(openInNewTab, deleteBookmark);
            btnChip.setContextMenu(contextMenu);

            bookmarksBar.getChildren().add(btnChip);
        }
    }

    public void toggleBookmarksBar() {
        boolean isVisible = !bookmarksBar.isVisible();
        bookmarksBar.setVisible(isVisible);
        bookmarksBar.setManaged(isVisible);
        settingsDAO.setShowBookmarksBar(isVisible);
    }

    public BrowserTab createNewTab(String url) {
        BrowserTab tab = new BrowserTab(this, url);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }

    public void closeTab(BrowserTab tab) {
        tabPane.getTabs().remove(tab);
        if (tabPane.getTabs().isEmpty()) {
            createNewTab(null);
        }
    }

    public BrowserTab getActiveTab() {
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        if (selected instanceof BrowserTab) {
            return (BrowserTab) selected;
        }
        return null;
    }

    private void openNewWindow() {
        Main newApp = new Main();
        Stage newStage = new Stage();
        try {
            newApp.start(newStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleTheme() {
        isDarkMode = !isDarkMode;
        settingsDAO.setTheme(isDarkMode ? "dark" : "light");
        if (primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
        // Giữ nguyên trang web hiện tại và lập tức đổi màu trang đó theo theme sáng/tối
        for (Tab t : tabPane.getTabs()) {
            if (t instanceof BrowserTab bt) {
                bt.applyDarkMode(isDarkMode);
            }
        }
    }

    private void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        URL cssResource = getClass().getResource("/css/chrome.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }

        rootPane.getStyleClass().removeAll("theme-light", "theme-dark");
        if (isDarkMode) {
            rootPane.getStyleClass().add("theme-dark");
        } else {
            rootPane.getStyleClass().add("theme-light");
        }
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN), () -> createNewTab(null));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN), () -> {
            BrowserTab active = getActiveTab();
            if (active != null) closeTab(active);
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), () -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.reload();
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F5), () -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.reload();
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN), () -> historyView.show());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), this::toggleBookmarksBar);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), () -> bookmarkView.show());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), () -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.toggleBookmark();
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), () -> {
            BrowserTab active = getActiveTab();
            if (active != null) {
                active.getUrlField().requestFocus();
                active.getUrlField().selectAll();
            }
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN), () -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.zoomIn();
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN), () -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.zoomIn();
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN), () -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.zoomOut();
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN), () -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.resetZoom();
        });
    }

    public void showNotification(String title, String message) {
        Platform.runLater(() -> {
            HBox toast = new HBox(10);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPadding(new Insets(10, 16, 10, 16));
            toast.setStyle("-fx-background-color: #323232; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 2);");

            Label lbl = new Label(title + ": " + message);
            lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

            toast.getChildren().add(lbl);
            toastContainer.getChildren().add(toast);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
            pause.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(ev -> toastContainer.getChildren().remove(toast));
                fadeOut.play();
            });
            pause.play();
        });
    }

    private void showSettingsDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cài đặt MiniBrowser");
        dialog.setHeaderText("Tùy chỉnh cấu hình trình duyệt");
        dialog.initOwner(primaryStage);

        ButtonType btnSave = new ButtonType("Lưu cài đặt", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20));

        TextField txtHomepage = new TextField(homepageUrl);
        txtHomepage.setPrefWidth(300);

        ComboBox<String> cbSearch = new ComboBox<>();
        cbSearch.getItems().addAll("Google (https://www.google.com/search?q=)", "Bing (https://www.bing.com/search?q=)", "DuckDuckGo (https://duckduckgo.com/?q=)");
        if (searchEngineUrl.contains("bing")) {
            cbSearch.setValue("Bing (https://www.bing.com/search?q=)");
        } else if (searchEngineUrl.contains("duckduckgo")) {
            cbSearch.setValue("DuckDuckGo (https://duckduckgo.com/?q=)");
        } else {
            cbSearch.setValue("Google (https://www.google.com/search?q=)");
        }

        CheckBox chkBookmarks = new CheckBox("Luôn hiển thị thanh dấu trang");
        chkBookmarks.setSelected(bookmarksBar.isVisible());

        grid.add(new Label("Trang chủ:"), 0, 0);
        grid.add(txtHomepage, 1, 0);
        grid.add(new Label("Công cụ tìm kiếm:"), 0, 1);
        grid.add(cbSearch, 1, 1);
        grid.add(new Label("Thanh dấu trang:"), 0, 2);
        grid.add(chkBookmarks, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSave) {
            String hp = txtHomepage.getText().trim();
            if (!hp.isEmpty()) {
                this.homepageUrl = hp;
                settingsDAO.setHomepage(hp);
            }

            String searchVal = cbSearch.getValue();
            if (searchVal.contains("bing")) {
                this.searchEngineUrl = "https://www.bing.com/search?q=";
            } else if (searchVal.contains("duckduckgo")) {
                this.searchEngineUrl = "https://duckduckgo.com/?q=";
            } else {
                this.searchEngineUrl = "https://www.google.com/search?q=";
            }
            settingsDAO.setSearchEngine(this.searchEngineUrl);

            boolean show = chkBookmarks.isSelected();
            bookmarksBar.setVisible(show);
            bookmarksBar.setManaged(show);
            settingsDAO.setShowBookmarksBar(show);

            showNotification("Cài đặt", "Đã lưu cấu hình thành công!");
        }
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Giới thiệu MiniBrowser");
        alert.setHeaderText("MiniBrowser - Đồ án Lab 7 (Lập trình mạng)");
        alert.setContentText("""
                Phiên bản: 1.0.0
                Kiến trúc: JavaFX Web (WebView, WebEngine) + SQLite JDBC
                Đặc điểm nổi bật:
                  • Trình duyệt đa tab độc lập, tối ưu hiệu năng và tài nguyên
                  • Trang chủ Start Page tích hợp đồng hồ thời gian thực và Speed Dial
                  • Thanh địa chỉ Omnibox thông minh: tự phân biệt URL và tìm kiếm đa động cơ
                  • Quản lý Dấu trang (Bookmarks) và thanh lối tắt nhanh
                  • Lưu trữ và tìm kiếm Lịch sử duyệt web (Browsing History) qua SQLite
                  • Hỗ trợ Giao diện Sáng (Light) & Tối (Dark)
                  • Hệ thống phím tắt tiêu chuẩn (Ctrl+T, Ctrl+W, Ctrl+H, Ctrl+D...)
                """);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public String getSearchEngineUrl() {
        return searchEngineUrl;
    }

    public static void main(String[] args) {
        minibrowser.Launcher.silenceWebKitMediaWarnings();
        launch(args);
    }
}

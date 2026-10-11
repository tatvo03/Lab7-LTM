package minibrowser.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import minibrowser.dao.BookmarkDAO;
import minibrowser.dao.HistoryDAO;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BrowserTab extends Tab {

    private final Main mainApp;
    private final BookmarkDAO bookmarkDAO;
    private final HistoryDAO historyDAO;

    private WebView webView;
    private WebEngine webEngine;
    private TextField urlField;
    private Button btnBack;
    private Button btnForward;
    private Button btnReload;
    private Button btnHome;
    private Button btnStar;
    private ProgressBar progressBar;
    private Label lblStatus;
    private Label tabTitleLabel;

    private double currentZoom = 1.0;
    private boolean isCustomNewTabPage = false;

    public BrowserTab(Main mainApp, String initialUrl) {
        this.mainApp = mainApp;
        this.bookmarkDAO = new BookmarkDAO();
        this.historyDAO = new HistoryDAO();

        initializeUI();
        setupWebEngine();

        if (initialUrl == null || initialUrl.trim().isEmpty() || initialUrl.equals("browser://newtab") || initialUrl.equals("chrome://newtab") || initialUrl.equals("about:blank")) {
            loadNewTabPage();
        } else {
            loadUrl(initialUrl);
        }
    }

    private void initializeUI() {
        // Tab Header: Title Label + Close Button
        HBox tabHeader = new HBox(6);
        tabHeader.setAlignment(Pos.CENTER_LEFT);
        tabHeader.setMaxWidth(180);

        Label tabIcon = new Label("🌐");
        tabIcon.setStyle("-fx-font-size: 11px;");

        tabTitleLabel = new Label("Tab mới");
        tabTitleLabel.setMaxWidth(130);
        tabTitleLabel.setStyle("-fx-font-size: 12px; -fx-text-overrun: ellipsis; -fx-font-weight: bold; -fx-text-fill: -text-main;");

        Button btnCloseTab = new Button("✕");
        btnCloseTab.setStyle("-fx-background-color: transparent; -fx-padding: 2 4 2 4; -fx-font-size: 10px; -fx-cursor: hand; -fx-text-fill: #5f6368; -fx-background-radius: 10px;");
        btnCloseTab.setOnMouseEntered(e -> btnCloseTab.setStyle("-fx-background-color: #dadce0; -fx-padding: 2 4 2 4; -fx-font-size: 10px; -fx-cursor: hand; -fx-text-fill: #202124; -fx-background-radius: 10px;"));
        btnCloseTab.setOnMouseExited(e -> btnCloseTab.setStyle("-fx-background-color: transparent; -fx-padding: 2 4 2 4; -fx-font-size: 10px; -fx-cursor: hand; -fx-text-fill: #5f6368; -fx-background-radius: 10px;"));
        btnCloseTab.setOnAction(e -> mainApp.closeTab(this));

        tabHeader.getChildren().addAll(tabIcon, tabTitleLabel, btnCloseTab);
        setGraphic(tabHeader);
        setText("");

        // Controls
        btnBack = createIconButton("←", "Quay lại (Alt+Left)");
        btnForward = createIconButton("→", "Tiến tới (Alt+Right)");
        btnReload = createIconButton("🔄", "Tải lại trang (Ctrl+R)");
        btnHome = createIconButton("🏠", "Trang chủ");

        btnBack.setDisable(true);
        btnForward.setDisable(true);

        btnBack.setOnAction(e -> goBack());
        btnForward.setOnAction(e -> goForward());
        btnReload.setOnAction(e -> {
            if (btnReload.getText().equals("✕")) {
                webEngine.getLoadWorker().cancel();
            } else {
                reload();
            }
        });
        btnHome.setOnAction(e -> goHome());

        // Omnibox (Address Bar)
        HBox omnibox = new HBox(6);
        omnibox.setAlignment(Pos.CENTER_LEFT);
        omnibox.getStyleClass().add("omnibox");
        HBox.setHgrow(omnibox, Priority.ALWAYS);

        Label lblLock = new Label("🔒");
        lblLock.setStyle("-fx-font-size: 12px; -fx-text-fill: #5f6368;");

        urlField = new TextField();
        urlField.setPromptText("Tìm kiếm hoặc nhập địa chỉ web (URL)...");
        urlField.getStyleClass().add("url-input");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        urlField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String input = urlField.getText();
                loadSmartUrl(input);
            }
        });

        urlField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Platform.runLater(() -> urlField.selectAll());
            }
        });

        btnStar = new Button("☆");
        btnStar.getStyleClass().add("star-button");
        btnStar.setTooltip(new Tooltip("Thêm trang này vào dấu trang (Ctrl+D)"));
        btnStar.setOnAction(e -> toggleBookmark());

        omnibox.getChildren().addAll(lblLock, urlField, btnStar);

        // Navigation Bar
        HBox navBar = new HBox(8);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(6, 12, 6, 12));
        navBar.getStyleClass().add("nav-bar");
        navBar.getChildren().addAll(btnBack, btnForward, btnReload, btnHome, omnibox);

        // Loading Progress Bar
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(2.5);
        progressBar.setMinHeight(2.5);
        progressBar.getStyleClass().add("chrome-progress-bar");
        progressBar.setVisible(false);

        // Web view
        webView = new WebView();
        webEngine = webView.getEngine();
        VBox.setVgrow(webView, Priority.ALWAYS);

        // Status bar
        lblStatus = new Label();
        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-main; -fx-padding: 3 10 3 10; -fx-background-color: -bg-chrome-bar; -fx-border-color: -border-color; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        lblStatus.setVisible(false);

        StackPane contentPane = new StackPane();
        contentPane.getChildren().addAll(webView, lblStatus);
        StackPane.setAlignment(lblStatus, Pos.BOTTOM_LEFT);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        // Main content VBox
        VBox root = new VBox();
        root.getChildren().addAll(navBar, progressBar, contentPane);
        setContent(root);
    }

    private Button createIconButton(String icon, String tooltipText) {
        Button btn = new Button(icon);
        btn.getStyleClass().add("chrome-icon-button");
        btn.setTooltip(new Tooltip(tooltipText));
        return btn;
    }

    private void setupWebEngine() {
        // Giả lập User-Agent Microsoft Edge chuẩn trên Windows để tương thích tối đa
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0");
        webEngine.setJavaScriptEnabled(true);

        // Location change
        webEngine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl != null) {
                // Tự động bỏ qua màn hình reCAPTCHA của Google bằng cách chuyển hướng sang Bing
                if (newUrl.contains("google.com/sorry/index")) {
                    String query = extractQueryFromSorryUrl(newUrl);
                    Platform.runLater(() -> {
                        mainApp.showNotification("Bỏ qua xác minh", "Đã tự động chuyển sang Bing để tránh mã xác nhận người máy!");
                        loadUrl("https://www.bing.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
                    });
                    return;
                }

                if (newUrl.startsWith("http://") || newUrl.startsWith("https://") || newUrl.startsWith("file://")) {
                    isCustomNewTabPage = false;
                    urlField.setText(newUrl);
                    updateStarButton(newUrl);
                }
            }
        });

        // Title change
        webEngine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                String truncated = newTitle.length() > 20 ? newTitle.substring(0, 18) + "..." : newTitle;
                tabTitleLabel.setText(truncated);
                setTooltip(new Tooltip(newTitle + "\n" + webEngine.getLocation()));
            } else {
                tabTitleLabel.setText("Tab mới");
            }
        });

        // Load progress & state
        Worker<Void> loadWorker = webEngine.getLoadWorker();
        progressBar.progressProperty().bind(loadWorker.progressProperty());

        loadWorker.stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.RUNNING) {
                progressBar.setVisible(true);
                btnReload.setText("✕");
                btnReload.getTooltip().setText("Dừng tải trang");
            } else {
                progressBar.setVisible(false);
                btnReload.setText("🔄");
                btnReload.getTooltip().setText("Tải lại trang (Ctrl+R)");

                updateHistoryButtons();

                if (newState == Worker.State.SUCCEEDED) {
                    String currentUrl = webEngine.getLocation();
                    String title = webEngine.getTitle();
                    if (title == null || title.trim().isEmpty()) {
                        title = currentUrl;
                    }

                    if (!isCustomNewTabPage() && currentUrl != null && (currentUrl.startsWith("http://") || currentUrl.startsWith("https://") || currentUrl.startsWith("file://"))) {
                        historyDAO.addHistory(title, currentUrl);
                        updateStarButton(currentUrl);

                        // Tự động đổi màu trang web sang Dark Mode nếu đang bật giao diện tối
                        if (mainApp.isDarkMode()) {
                            applyDarkMode(true);
                        }
                    }
                }
            }
        });

        // Status / Message listener (hovering over links)
        loadWorker.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            if (newMsg != null && !newMsg.isEmpty() && !newMsg.equals("Loading " + webEngine.getLocation())) {
                lblStatus.setText(newMsg);
                lblStatus.setVisible(true);
            } else {
                lblStatus.setVisible(false);
            }
        });

        // Handle JS Alert
        webEngine.setOnAlert(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo từ trang web");
            alert.setHeaderText(null);
            alert.setContentText(event.getData());
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();
        });

        // Handle JS Confirm
        webEngine.setConfirmHandler(message -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận từ trang web");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(mainApp.getPrimaryStage());
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        });

        // Handle JS Prompt
        webEngine.setPromptHandler(promptData -> {
            TextInputDialog dialog = new TextInputDialog(promptData.getDefaultValue());
            dialog.setTitle("Yêu cầu nhập từ trang web");
            dialog.setHeaderText(promptData.getMessage());
            dialog.initOwner(mainApp.getPrimaryStage());
            Optional<String> result = dialog.showAndWait();
            return result.orElse(null);
        });

        // Handle Popup / target="_blank" links -> open new tab!
        webEngine.setCreatePopupHandler(config -> {
            BrowserTab newTab = mainApp.createNewTab(null);
            return newTab.getWebEngine();
        });
    }

    public void loadSmartUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            loadNewTabPage();
            return;
        }

        String trimmed = input.trim();

        if (trimmed.equalsIgnoreCase("browser://newtab") || trimmed.equalsIgnoreCase("chrome://newtab") || trimmed.equalsIgnoreCase("about:blank")) {
            loadNewTabPage();
            return;
        }

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://") || trimmed.startsWith("data:")) {
            loadUrl(trimmed);
            return;
        }

        // Check if looks like domain name
        boolean isDomain = trimmed.matches("^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+(:[0-9]+)?(/.*)?$");
        if (isDomain || trimmed.startsWith("localhost")) {
            loadUrl("https://" + trimmed);
        } else if (trimmed.equalsIgnoreCase("vnexpress")) {
            loadUrl("https://vnexpress.net");
        } else if (trimmed.equalsIgnoreCase("youtube")) {
            loadUrl("https://www.youtube.com");
        } else if (trimmed.equalsIgnoreCase("google")) {
            loadUrl("https://www.google.com");
        } else if (trimmed.equalsIgnoreCase("facebook")) {
            loadUrl("https://www.facebook.com");
        } else if (trimmed.equalsIgnoreCase("dantri")) {
            loadUrl("https://dantri.com.vn");
        } else {
            // Search using configured search engine
            String searchUrl = mainApp.getSearchEngineUrl() + URLEncoder.encode(trimmed, StandardCharsets.UTF_8);
            loadUrl(searchUrl);
        }
    }

    private String extractQueryFromSorryUrl(String url) {
        try {
            String decoded = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
            int qIdx = decoded.indexOf("q=");
            if (qIdx != -1) {
                int start = qIdx + 2;
                int end = decoded.indexOf('&', start);
                return end != -1 ? decoded.substring(start, end) : decoded.substring(start);
            }
        } catch (Exception ignored) {}
        return "tin tức";
    }

    public void loadUrl(String url) {
        isCustomNewTabPage = false;
        urlField.setText(url);
        webEngine.load(url);
    }

    public void loadNewTabPage() {
        isCustomNewTabPage = true;
        urlField.setText("");
        tabTitleLabel.setText("Tab mới");
        btnStar.setText("☆");
        btnStar.setStyle("-fx-text-fill: #5f6368;");
        webEngine.loadContent(NewTabPage.getHtml(mainApp.isDarkMode(), mainApp.getSearchEngineUrl()), "text/html");
    }

    public void goBack() {
        WebHistory history = webEngine.getHistory();
        if (history.getCurrentIndex() > 0) {
            history.go(-1);
            updateHistoryButtons();
        }
    }

    public void goForward() {
        WebHistory history = webEngine.getHistory();
        if (history.getCurrentIndex() < history.getEntries().size() - 1) {
            history.go(1);
            updateHistoryButtons();
        }
    }

    public void reload() {
        if (isCustomNewTabPage) {
            loadNewTabPage();
        } else {
            webEngine.reload();
        }
    }

    public void goHome() {
        String homepage = mainApp.getHomepageUrl();
        if (homepage == null || homepage.isEmpty() || homepage.equals("chrome://newtab")) {
            loadNewTabPage();
        } else {
            loadUrl(homepage);
        }
    }

    private void updateHistoryButtons() {
        WebHistory history = webEngine.getHistory();
        int currentIndex = history.getCurrentIndex();
        btnBack.setDisable(currentIndex <= 0);
        btnForward.setDisable(currentIndex >= history.getEntries().size() - 1);
    }

    public void updateStarButton(String url) {
        if (url == null || url.isEmpty() || isCustomNewTabPage) {
            btnStar.setText("☆");
            btnStar.setStyle("-fx-text-fill: #5f6368;");
            return;
        }

        boolean bookmarked = bookmarkDAO.isBookmarked(url);
        if (bookmarked) {
            btnStar.setText("★");
            btnStar.setStyle("-fx-text-fill: #fbbc04; -fx-font-size: 16px;");
        } else {
            btnStar.setText("☆");
            btnStar.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 16px;");
        }
    }

    public void toggleBookmark() {
        String currentUrl = webEngine.getLocation();
        if (isCustomNewTabPage || currentUrl == null || currentUrl.isEmpty() || currentUrl.startsWith("data:")) {
            return;
        }

        boolean currentlyBookmarked = bookmarkDAO.isBookmarked(currentUrl);
        String title = webEngine.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = currentUrl;
        }

        if (currentlyBookmarked) {
            bookmarkDAO.deleteBookmarkByUrl(currentUrl);
            updateStarButton(currentUrl);
            mainApp.showNotification("Đã xóa khỏi Dấu trang", "Đã gỡ bỏ: " + title);
        } else {
            bookmarkDAO.addBookmark(title, currentUrl);
            updateStarButton(currentUrl);
            mainApp.showNotification("Đã thêm vào Dấu trang", "Đã lưu: " + title);
        }

        mainApp.refreshBookmarksBar();
    }

    public void zoomIn() {
        currentZoom = Math.min(2.5, currentZoom + 0.15);
        webView.setZoom(currentZoom);
    }

    public void zoomOut() {
        currentZoom = Math.max(0.4, currentZoom - 0.15);
        webView.setZoom(currentZoom);
    }

    public void resetZoom() {
        currentZoom = 1.0;
        webView.setZoom(currentZoom);
    }

    public WebView getWebView() {
        return webView;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    public TextField getUrlField() {
        return urlField;
    }

    public String getCurrentUrl() {
        return webEngine.getLocation();
    }

    public String getCurrentTitle() {
        return webEngine.getTitle();
    }

    public boolean isCustomNewTabPage() {
        String loc = webEngine.getLocation();
        if (loc == null || loc.isEmpty() || loc.equals("about:blank") || loc.startsWith("data:")) {
            return true;
        }
        return isCustomNewTabPage;
    }

    public void applyDarkMode(boolean dark) {
        if (isCustomNewTabPage()) {
            loadNewTabPage();
            return;
        }

        try {
            if (dark) {
                String script = """
                    (function() {
                        let style = document.getElementById('minibrowser-dark-mode');
                        if (!style) {
                            style = document.createElement('style');
                            style.id = 'minibrowser-dark-mode';
                            (document.head || document.documentElement).appendChild(style);
                        }
                        style.innerHTML = `
                            html {
                                background-color: #ffffff !important;
                                filter: invert(100%) hue-rotate(180deg) contrast(110%) brightness(95%) !important;
                            }
                            body {
                                background-color: #ffffff !important;
                            }
                            /* Phục hồi màu sắc tự nhiên CHỈ cho ảnh và video */
                            img, video, iframe {
                                filter: invert(100%) hue-rotate(180deg) !important;
                            }
                            /* Giữ nguyên và làm sáng rõ toàn bộ logo, icon, ký hiệu và biểu tượng vector (SVG) */
                            svg, svg *, 
                            [class*="logo"], [class*="icon"], 
                            yt-icon, yt-icon *, ytd-topbar-logo-renderer *,
                            img[class*="logo"], img[class*="icon"],
                            [class*="logo"] img, [class*="icon"] img {
                                filter: none !important;
                            }
                        `;
                    })();
                """;
                webEngine.executeScript(script);
            } else {
                String script = """
                    (function() {
                        let style = document.getElementById('minibrowser-dark-mode');
                        if (style) {
                            style.remove();
                        }
                    })();
                """;
                webEngine.executeScript(script);
            }
        } catch (Exception ignored) {
        }
    }
}

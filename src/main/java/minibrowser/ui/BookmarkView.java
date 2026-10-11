package minibrowser.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import minibrowser.dao.BookmarkDAO;
import minibrowser.model.BookmarkItem;

import java.util.Optional;

public class BookmarkView {

    private final Main mainApp;
    private final BookmarkDAO bookmarkDAO;
    private final ObservableList<BookmarkItem> bookmarkList;
    private TableView<BookmarkItem> tableView;
    private Stage stage;

    public BookmarkView(Main mainApp) {
        this.mainApp = mainApp;
        this.bookmarkDAO = new BookmarkDAO();
        this.bookmarkList = FXCollections.observableArrayList();
    }

    public void show() {
        if (stage != null && stage.isShowing()) {
            stage.toFront();
            refreshData();
            return;
        }

        stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.initOwner(mainApp.getPrimaryStage());
        stage.setTitle("Trình quản lý Dấu trang - MiniBrowser");

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header Bar
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("⭐ Quản lý Dấu trang");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Tìm kiếm dấu trang...");
        txtSearch.setPrefWidth(220);
        txtSearch.setStyle("-fx-background-radius: 16px; -fx-padding: 6 12 6 12;");
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                refreshData();
            } else {
                bookmarkList.setAll(bookmarkDAO.searchBookmarks(newVal));
            }
        });

        Button btnAdd = new Button("+ Thêm dấu trang");
        btnAdd.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> showAddEditDialog(null));

        header.getChildren().addAll(lblTitle, spacer, txtSearch, btnAdd);

        // Table View
        tableView = new TableView<>();
        tableView.setItems(bookmarkList);
        tableView.setPlaceholder(new Label("Chưa có dấu trang nào"));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<BookmarkItem, String> colTitle = new TableColumn<>("Tiêu đề");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(220);

        TableColumn<BookmarkItem, String> colUrl = new TableColumn<>("Đường dẫn (URL)");
        colUrl.setCellValueFactory(new PropertyValueFactory<>("url"));
        colUrl.setPrefWidth(340);

        TableColumn<BookmarkItem, String> colDate = new TableColumn<>("Thời gian thêm");
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colDate.setPrefWidth(140);

        tableView.getColumns().addAll(colTitle, colUrl, colDate);

        // Double click to open
        tableView.setRowFactory(tv -> {
            TableRow<BookmarkItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openSelectedBookmark(row.getItem());
                }
            });
            return row;
        });

        // Bottom action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnOpen = new Button("Mở trong Tab mới");
        btnOpen.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: #1a73e8; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        btnOpen.setOnAction(e -> {
            BookmarkItem selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openSelectedBookmark(selected);
            }
        });

        Button btnEdit = new Button("Chỉnh sửa");
        btnEdit.setStyle("-fx-background-color: #f1f3f4; -fx-text-fill: #3c4043; -fx-background-radius: 6px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> {
            BookmarkItem selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddEditDialog(selected);
            }
        });

        Button btnDelete = new Button("Xóa");
        btnDelete.setStyle("-fx-background-color: #fce8e6; -fx-text-fill: #d93025; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            BookmarkItem selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa dấu trang: " + selected.getTitle() + "?", ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Xác nhận xóa dấu trang");
                confirm.setHeaderText(null);
                confirm.initOwner(stage);
                Optional<ButtonType> res = confirm.showAndWait();
                if (res.isPresent() && res.get() == ButtonType.YES) {
                    bookmarkDAO.deleteBookmark(selected.getId());
                    refreshData();
                    mainApp.refreshBookmarksBar();
                }
            }
        });

        Button btnClose = new Button("Đóng");
        btnClose.setStyle("-fx-background-color: #f1f3f4; -fx-text-fill: #5f6368; -fx-background-radius: 6px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        btnClose.setOnAction(e -> stage.close());

        actions.getChildren().addAll(btnOpen, btnEdit, btnDelete, btnClose);

        root.getChildren().addAll(header, tableView, actions);

        Scene scene = new Scene(root, 760, 480);
        stage.setScene(scene);
        refreshData();
        stage.show();
    }

    private void refreshData() {
        bookmarkList.setAll(bookmarkDAO.getAllBookmarks());
    }

    private void openSelectedBookmark(BookmarkItem item) {
        if (item != null && item.getUrl() != null) {
            mainApp.createNewTab(item.getUrl());
            stage.close();
        }
    }

    private void showAddEditDialog(BookmarkItem existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm dấu trang" : "Sửa dấu trang");
        dialog.setHeaderText(existing == null ? "Nhập thông tin dấu trang mới:" : "Chỉnh sửa thông tin dấu trang:");
        dialog.initOwner(stage);

        ButtonType btnSaveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField txtTitle = new TextField(existing != null ? existing.getTitle() : "");
        txtTitle.setPromptText("Tên dấu trang");
        TextField txtUrl = new TextField(existing != null ? existing.getUrl() : "https://");
        txtUrl.setPromptText("https://example.com");

        grid.add(new Label("Tiêu đề:"), 0, 0);
        grid.add(txtTitle, 1, 0);
        grid.add(new Label("URL:"), 0, 1);
        grid.add(txtUrl, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnSaveType) {
            String title = txtTitle.getText().trim();
            String url = txtUrl.getText().trim();
            if (!url.isEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                if (title.isEmpty()) {
                    title = url;
                }

                if (existing != null) {
                    bookmarkDAO.deleteBookmark(existing.getId());
                }
                bookmarkDAO.addBookmark(title, url);
                refreshData();
                mainApp.refreshBookmarksBar();
            }
        }
    }
}

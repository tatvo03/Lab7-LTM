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
import minibrowser.dao.HistoryDAO;
import minibrowser.model.HistoryItem;

import java.util.Optional;

public class HistoryView {

    private final Main mainApp;
    private final HistoryDAO historyDAO;
    private final ObservableList<HistoryItem> historyList;
    private TableView<HistoryItem> tableView;
    private Stage stage;

    public HistoryView(Main mainApp) {
        this.mainApp = mainApp;
        this.historyDAO = new HistoryDAO();
        this.historyList = FXCollections.observableArrayList();
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
        stage.setTitle("Lịch sử duyệt web - MiniBrowser");

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("📜 Lịch sử duyệt web");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Tìm trong lịch sử...");
        txtSearch.setPrefWidth(240);
        txtSearch.setStyle("-fx-background-radius: 16px; -fx-padding: 6 12 6 12;");
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                refreshData();
            } else {
                historyList.setAll(historyDAO.searchHistory(newVal));
            }
        });

        Button btnClearAll = new Button("🗑️ Xóa dữ liệu duyệt web");
        btnClearAll.setStyle("-fx-background-color: #fce8e6; -fx-text-fill: #d93025; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        btnClearAll.setOnAction(e -> clearAllHistory());

        header.getChildren().addAll(lblTitle, spacer, txtSearch, btnClearAll);

        // Table
        tableView = new TableView<>();
        tableView.setItems(historyList);
        tableView.setPlaceholder(new Label("Không có lịch sử duyệt web nào"));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<HistoryItem, String> colTime = new TableColumn<>("Thời gian");
        colTime.setCellValueFactory(new PropertyValueFactory<>("visitedAt"));
        colTime.setPrefWidth(150);

        TableColumn<HistoryItem, String> colTitle = new TableColumn<>("Tiêu đề trang");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(250);

        TableColumn<HistoryItem, String> colUrl = new TableColumn<>("Đường dẫn (URL)");
        colUrl.setCellValueFactory(new PropertyValueFactory<>("url"));
        colUrl.setPrefWidth(330);

        tableView.getColumns().addAll(colTime, colTitle, colUrl);

        tableView.setRowFactory(tv -> {
            TableRow<HistoryItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openSelectedHistory(row.getItem());
                }
            });
            return row;
        });

        // Bottom Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnOpen = new Button("Mở liên kết");
        btnOpen.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: #1a73e8; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        btnOpen.setOnAction(e -> {
            HistoryItem selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openSelectedHistory(selected);
            }
        });

        Button btnDelete = new Button("Xóa mục đã chọn");
        btnDelete.setStyle("-fx-background-color: #f1f3f4; -fx-text-fill: #3c4043; -fx-background-radius: 6px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            HistoryItem selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                historyDAO.deleteHistory(selected.getId());
                refreshData();
            }
        });

        Button btnClose = new Button("Đóng");
        btnClose.setStyle("-fx-background-color: #f1f3f4; -fx-text-fill: #5f6368; -fx-background-radius: 6px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        btnClose.setOnAction(e -> stage.close());

        actions.getChildren().addAll(btnOpen, btnDelete, btnClose);

        root.getChildren().addAll(header, tableView, actions);

        Scene scene = new Scene(root, 780, 500);
        stage.setScene(scene);
        refreshData();
        stage.show();
    }

    private void refreshData() {
        historyList.setAll(historyDAO.getAllHistory());
    }

    private void openSelectedHistory(HistoryItem item) {
        if (item != null && item.getUrl() != null) {
            mainApp.createNewTab(item.getUrl());
            stage.close();
        }
    }

    private void clearAllHistory() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa toàn bộ lịch sử duyệt web không?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa toàn bộ lịch sử");
        confirm.setHeaderText(null);
        confirm.initOwner(stage);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            historyDAO.clearHistory();
            refreshData();
            mainApp.showNotification("Lịch sử", "Đã xóa toàn bộ lịch sử duyệt web thành công!");
        }
    }
}

package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.UserSession;

public class DashboardOrangTuaController {

    @FXML private StackPane contentPane;
    @FXML private Button dashboardButton;
    @FXML private Button parentInsightButton;
    @FXML private Button tantanganButton;
    @FXML private Button profilButton;

    private List<Button> navigationButtons;

    @FXML
    private void initialize() {
        navigationButtons = new ArrayList<>();
        navigationButtons.add(dashboardButton);
        navigationButtons.add(parentInsightButton);
        navigationButtons.add(tantanganButton);
        navigationButtons.add(profilButton);

        showDashboard(); 
    }

    private void setActiveButton(Button activeButton) {
        for (Button button : navigationButtons) {
            if (button != null) {
                button.getStyleClass().remove("sidebar-button-active");
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardOrangTuaContentController) {
                ((DashboardOrangTuaContentController) controller).setMainController(this);
            }

            contentPane.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Gagal memuat halaman: " + fxmlPath, e);
        }
    }

    @FXML
    public void showDashboard() {
        loadView("/view/dashboard_orangtua_content.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML
    public void showParentInsight() {
        loadView("/view/parent_insight.fxml");
        setActiveButton(parentInsightButton);
    }

    @FXML
    public void showTantangan() {
        loadView("/view/tantangan.fxml");
        setActiveButton(tantanganButton);
    }

    @FXML
    public void showProfil() {
        loadView("/view/profil.fxml");
        setActiveButton(profilButton);
    }

    @FXML
    private void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin logout?", ButtonType.OK, ButtonType.CANCEL);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    UserSession.clearSession();
                    Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                    Stage stage = (Stage) contentPane.getScene().getWindow();
                    Scene scene = new Scene(loginRoot, stage.getWidth(), stage.getHeight());
                    URL cssResource = getClass().getResource("/style.css");
                    if (cssResource != null) {
                        scene.getStylesheets().add(cssResource.toExternalForm());
                    }
                    stage.setScene(scene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showErrorAlert(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n\nDetail: " + e.getMessage());
        alert.showAndWait();
    }
}
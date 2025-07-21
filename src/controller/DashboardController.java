package controller;

import java.io.IOException;
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
import java.net.URL;
import model.UserSession;

public class DashboardController {

    @FXML private StackPane contentPane;
    @FXML private Button dashboardButton;
    @FXML private Button moodTrackerButton;
    @FXML private Button tantanganButton;
    @FXML private Button jurnalButton;
    @FXML private Button profilButton;

    private List<Button> navigationButtons;

    @FXML
    private void initialize() {
        navigationButtons = new ArrayList<>();
        navigationButtons.add(dashboardButton);
        navigationButtons.add(moodTrackerButton);
        navigationButtons.add(tantanganButton);
        navigationButtons.add(jurnalButton);
        navigationButtons.add(profilButton);
        showDashboard();
    }

    private void setActiveButton(Button activeButton) {
        for (Button button : navigationButtons) {
            button.getStyleClass().remove("sidebar-button-active");
        }
        activeButton.getStyleClass().add("sidebar-button-active");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardHomeViewController) {
                ((DashboardHomeViewController) controller).setMainController(this);
            }

            contentPane.getChildren().setAll(page);

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Gagal memuat halaman: " + fxmlPath, e);
        }
    }

    @FXML 
    public void showDashboard() {
        loadView("/view/dashboard_home.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML 
    public void showMoodTracker() {
        loadView("/view/mood_entry.fxml");
        setActiveButton(moodTrackerButton);
    }

    @FXML 
    public void showTantangan() {
        loadView("/view/tantangan.fxml");
        setActiveButton(tantanganButton);
    }

    @FXML 
    public void showJurnal() {
        loadView("/view/jurnal_harian.fxml");
        setActiveButton(jurnalButton);
    }

    @FXML 
    public void showProfil() {
        loadView("/view/profil.fxml");
        setActiveButton(profilButton);
    }

    @FXML
    private void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin logout?", ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Konfirmasi Logout");
        confirmation.setHeaderText(null);
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                UserSession.clearSession();
                Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                Stage stage = (Stage) contentPane.getScene().getWindow();
                Scene scene = new Scene(loginRoot, stage.getWidth(), stage.getHeight());
                URL cssResource = getClass().getResource("/style.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                } else {
                    System.err.println("Warning: File /style.css tidak ditemukan saat logout.");
                }
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                showErrorAlert("Gagal kembali ke halaman login.", e);
            }
        }
    }
    
    private void showErrorAlert(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n\nDetail: " + e.getMessage());
        alert.showAndWait();
    }
}
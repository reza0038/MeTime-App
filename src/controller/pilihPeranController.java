package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import model.User;
import model.UserManager;
import model.UserSession;

public class pilihPeranController {

    private void saveUserRoleAndLoadScene(ActionEvent event, String peran, String fxmlPath) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            currentUser.setPeran(peran);
            UserManager.updateUser(currentUser); 
        }
        loadScene(event, fxmlPath);
    }

    @FXML
    private void handlePilihRemaja(ActionEvent event) {
        saveUserRoleAndLoadScene(event, "Remaja", "/view/dashboard.fxml");
    }

    @FXML
    private void handlePilihOrangTua(ActionEvent event) {
        saveUserRoleAndLoadScene(event, "Orang Tua", "/view/dashboard_orangtua.fxml");
    }

    @FXML
    private void handlePilihPsikolog(ActionEvent event) {
        saveUserRoleAndLoadScene(event, "Psikolog", "/DashboardPsikolog.fxml");
    }

    private void loadScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight()); 
            
    
            URL cssResource = getClass().getResource("/style.css");
             if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                System.err.println("Warning: File /style.css tidak ditemukan.");
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
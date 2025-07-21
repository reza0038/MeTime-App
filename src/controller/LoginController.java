package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.UserManager;
import model.User;
import model.UserSession;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements javafx.fxml.Initializable {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private ImageView backgroundLogin;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messageLabel.setText("");
        UserManager.loadUsers();

        Platform.runLater(() -> {
            Scene scene = backgroundLogin.getScene();
            if (scene != null) {
                backgroundLogin.fitWidthProperty().bind(scene.widthProperty());
                backgroundLogin.fitHeightProperty().bind(scene.heightProperty());
            }
        });
    }

    @FXML
    private void handleLoginAction(javafx.event.ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        User user = UserManager.getUserByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            messageLabel.setText("Login berhasil!");
            messageLabel.setStyle("-fx-text-fill: green;");
            
            UserSession.setCurrentUser(user);

            
            String fxmlPath;
            String peran = user.getPeran();
            if (peran != null && !peran.isEmpty()) {
                switch (peran) {
                    case "Remaja":
                        fxmlPath = "/view/dashboard.fxml";
                        break;
                    case "Orang Tua":
                        fxmlPath = "/view/dashboard_orangtua.fxml"; 
                        break;
                    default:
                        fxmlPath = "/view/pilih_peran.fxml";
                        break;
                }
            } else {
                fxmlPath = "/view/pilih_peran.fxml";
            }
            
            loadNextScene(event, fxmlPath);

        } else {
            messageLabel.setText("Username atau password salah.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void handleBelumPunyaAkun(javafx.event.ActionEvent event) {
        loadNextScene(event, "/view/register.fxml");
    }

    private void loadNextScene(javafx.event.ActionEvent event, String fxmlPath) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                messageLabel.setText("Error: File FXML tidak ditemukan: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);

            
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            
            URL cssResource = getClass().getResource("/style.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Gagal membuka halaman berikutnya.");
        }
    }
}
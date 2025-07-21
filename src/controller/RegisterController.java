package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import model.UserManager;
import model.User;

import java.io.IOException;

import java.net.URL; 

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        messageLabel.setText("");
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("Semua kolom harus diisi.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!password.equals(confirm)) {
            messageLabel.setText("Password tidak cocok.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (UserManager.getUserByUsername(username) != null) {
            messageLabel.setText("Username sudah digunakan.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }


        User newUser = new User(username, email, password);
        

        UserManager.addUser(newUser);

        messageLabel.setText("Registrasi berhasil, silakan kembali ke halaman Login.");
        messageLabel.setStyle("-fx-text-fill: green;");
        
      
        usernameField.clear(); 
        emailField.clear(); 
        passwordField.clear(); 
        confirmPasswordField.clear();
    }

   @FXML
private void handleBackToLogin(javafx.event.ActionEvent event) {
    try {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        
        Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(loginRoot, stage.getWidth(), stage.getHeight());
        
        URL cssResource = getClass().getResource("/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }

        stage.setScene(scene);
        stage.show();
        
    } catch (IOException e) {
        e.printStackTrace();
    }
}
} 
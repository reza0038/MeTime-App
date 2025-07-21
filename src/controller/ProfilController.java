package controller;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox; 
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import model.User;
import model.Profil;
import model.UserManager;
import model.UserSession;

public class ProfilController {

    @FXML private Label usernameLabel;
    @FXML private TextField namaLengkapField;
    @FXML private DatePicker tanggalLahirPicker;
    @FXML private RadioButton lakiLakiRadio;
    @FXML private RadioButton perempuanRadio;
    @FXML private TextField emailField;
    @FXML private TextField teleponField;
    @FXML private TextField usernameWaliField;
    @FXML private Label messageLabel;
    @FXML private PasswordField passwordLamaField;
    @FXML private PasswordField passwordBaruField;
    @FXML private PasswordField konfirmasiPasswordBaruField;
    @FXML private VBox bagianWali; 

    private static final String PROFIL_FILE_BASE = "xml/profil_";
    private String currentUsername;
    private Profil currentProfile;
    private User currentUser;

    @FXML
    public void initialize() {
        ToggleGroup jenisKelaminGroup = new ToggleGroup();
        lakiLakiRadio.setToggleGroup(jenisKelaminGroup);
        perempuanRadio.setToggleGroup(jenisKelaminGroup);

        this.currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            currentUsername = currentUser.getUsername();
            usernameLabel.setText(currentUsername);
            loadProfile();

            if ("Remaja".equals(currentUser.getPeran())) {
                bagianWali.setVisible(true);
                bagianWali.setManaged(true);
            }

        } else {
            usernameLabel.setText("Tidak ada pengguna login.");
            showAlert("Error", "Tidak ada pengguna yang login. Profil tidak dapat dimuat.");
            setFormEditable(false);
        }
    }

    private void setFormEditable(boolean editable) {
        namaLengkapField.setEditable(editable);
        tanggalLahirPicker.setDisable(!editable);
        lakiLakiRadio.setDisable(!editable);
        perempuanRadio.setDisable(!editable);
        emailField.setEditable(editable);
        teleponField.setEditable(editable);
        usernameWaliField.setEditable(editable);
        passwordLamaField.setEditable(editable);
        passwordBaruField.setEditable(editable);
        konfirmasiPasswordBaruField.setEditable(editable);
    }

    private XStream createXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{Profil.class, LocalDate.class});
        xstream.alias("profil", Profil.class);
        return xstream;
    }

    private File getProfilFile(String username) {
        File file = new File(PROFIL_FILE_BASE + username + ".xml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        return file;
    }

    private void loadProfile() {
        File profilFile = getProfilFile(currentUsername);
        XStream xstream = createXStream();

        if (profilFile.exists()) {
            try (FileReader reader = new FileReader(profilFile)) {
                currentProfile = (Profil) xstream.fromXML(reader);
                populateForm();
                messageLabel.setText("Profil berhasil dimuat.");
            } catch (Exception e) {
                messageLabel.setText("Gagal memuat profil.");
                currentProfile = null;
            }
        } else {
            messageLabel.setText("Profil belum ada. Silakan isi.");
            currentProfile = null;
        }

        if (currentProfile == null) {
            resetForm();
        }
    }

    private void populateForm() {
        if (currentProfile != null) {
            namaLengkapField.setText(currentProfile.getNamaLengkap());
            tanggalLahirPicker.setValue(currentProfile.getTanggalLahir());
            String jenisKelamin = currentProfile.getJenisKelamin() != null ? currentProfile.getJenisKelamin() : "";
            if (jenisKelamin.equals("Laki-laki")) lakiLakiRadio.setSelected(true);
            else if (jenisKelamin.equals("Perempuan")) perempuanRadio.setSelected(true);
            emailField.setText(currentProfile.getAlamatEmail());
            teleponField.setText(currentProfile.getNomorTelepon());
            usernameWaliField.setText(currentProfile.getUsernameWali());
        }
    }

    @FXML
    private void handleSimpanProfil() {
        String namaLengkap = namaLengkapField.getText().trim();
        LocalDate tanggalLahir = tanggalLahirPicker.getValue();
        String jenisKelamin = lakiLakiRadio.isSelected() ? "Laki-laki" : (perempuanRadio.isSelected() ? "Perempuan" : "");
        String email = emailField.getText().trim();
        String telepon = teleponField.getText().trim();
        String usernameWali = usernameWaliField.getText().trim();

        if (namaLengkap.isEmpty() || tanggalLahir == null || jenisKelamin.isEmpty() || email.isEmpty()) {
            messageLabel.setText("Nama, Tanggal Lahir, Jenis Kelamin, dan Email harus diisi.");
            return;
        }
        
        if (currentProfile == null) {
            currentProfile = new Profil(currentUsername, namaLengkap, tanggalLahir, jenisKelamin, email, telepon, usernameWali);
        } else {
            currentProfile.setNamaLengkap(namaLengkap);
            currentProfile.setTanggalLahir(tanggalLahir);
            currentProfile.setJenisKelamin(jenisKelamin);
            currentProfile.setAlamatEmail(email);
            currentProfile.setNomorTelepon(telepon);
            currentProfile.setUsernameWali(usernameWali);
        }

        try (FileWriter writer = new FileWriter(getProfilFile(currentUsername))) {
            createXStream().toXML(currentProfile, writer);
            messageLabel.setText("Profil berhasil disimpan!");
        } catch (IOException e) {
            messageLabel.setText("Gagal menyimpan profil: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetForm() {
        resetForm();
    }

    private void resetForm() {
        namaLengkapField.clear();
        tanggalLahirPicker.setValue(null);
        lakiLakiRadio.setSelected(false);
        perempuanRadio.setSelected(false);
        teleponField.clear();
        usernameWaliField.clear();
        passwordLamaField.clear();
        passwordBaruField.clear();
        konfirmasiPasswordBaruField.clear();
        
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
        }
        messageLabel.setText("Form direset.");
    }
    
    @FXML
    private void handleGantiPassword() {
        String passLama = passwordLamaField.getText();
        String passBaru = passwordBaruField.getText();
        String konfirmasiPass = konfirmasiPasswordBaruField.getText();

        if (passLama.isEmpty() || passBaru.isEmpty() || konfirmasiPass.isEmpty()) {
            messageLabel.setText("Semua kolom kata sandi harus diisi.");
            return;
        }

        if (!currentUser.getPassword().equals(passLama)) {
            messageLabel.setText("Kata sandi saat ini salah.");
            return;
        }

        if (!passBaru.equals(konfirmasiPass)) {
            messageLabel.setText("Konfirmasi kata sandi baru tidak cocok.");
            return;
        }

        currentUser.setPassword(passBaru);
        UserManager.updateUser(currentUser);
        
        showAlert("Sukses", "Kata sandi berhasil diperbarui.");
        passwordLamaField.clear();
        passwordBaruField.clear();
        konfirmasiPasswordBaruField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
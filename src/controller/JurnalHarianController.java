package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font; 
import javafx.scene.text.FontWeight; 

import model.Jurnal; 
import model.User;
import model.JurnalManager; 
import model.UserSession;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class JurnalHarianController implements Initializable { 

    @FXML private ListView<Jurnal> jurnalListView;
    @FXML private Label tanggalDetailLabel;
    @FXML private TextArea isiJurnalArea;

    
    @FXML private RadioButton shareYes;
    @FXML private RadioButton shareNo;
    @FXML private ToggleGroup shareToggleGroup; 

    
    @FXML private Button simpanButton;
    @FXML private Button buatJurnalBaruButton; 
    @FXML private Button hapusButton;


    private ObservableList<Jurnal> jurnalEntries = FXCollections.observableArrayList();
    private Jurnal jurnalYangDipilih;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.currentUser = UserSession.getCurrentUser();
        if (this.currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error Sesi", "Sesi pengguna tidak ditemukan. Silakan login kembali.");
            if(jurnalListView != null) jurnalListView.setDisable(true);
            if(isiJurnalArea != null) isiJurnalArea.setDisable(true);
            if(simpanButton != null) simpanButton.setDisable(true);
            if(buatJurnalBaruButton != null) buatJurnalBaruButton.setDisable(true);
            if(hapusButton != null) hapusButton.setDisable(true);
            if(shareYes != null) shareYes.setDisable(true);
            if(shareNo != null) shareNo.setDisable(true);
            return;
        }

       
        if (shareToggleGroup == null) { 
            shareToggleGroup = new ToggleGroup();
        }
        shareYes.setToggleGroup(shareToggleGroup);
        shareNo.setToggleGroup(shareToggleGroup);
        shareNo.setSelected(true); 

        setupListView();
        loadJurnalEntries();
        handleBuatJurnalBaru(); 
    }
    
    private void setupListView() {
        jurnalListView.setCellFactory(lv -> new ListCell<Jurnal>() {
            @Override
            protected void updateItem(Jurnal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                    String preview = item.getIsi().length() > 25 ? item.getIsi().substring(0, 25).replace("\n", " ") + "..." : item.getIsi().replace("\n", " ");
                    
                    Label dateLabel = new Label(item.getTanggal().format(formatter));
                    dateLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                    
                    Label previewLabel = new Label(preview);
                    VBox cellContent = new VBox(5, dateLabel, previewLabel);
                    setGraphic(cellContent);
                }
            }
        });

        jurnalListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                jurnalYangDipilih = newSelection;
                tampilkanDetailJurnal(jurnalYangDipilih);
            }
        });
    }

    private void loadJurnalEntries() {
        List<Jurnal> userJurnals = JurnalManager.getJurnalsByUsername(currentUser.getUsername());
        jurnalEntries.setAll(userJurnals); 
        jurnalEntries.sort(Comparator.comparing(Jurnal::getTanggal).reversed());
        jurnalListView.setItems(jurnalEntries);
    }
    
    private void tampilkanDetailJurnal(Jurnal jurnal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
        tanggalDetailLabel.setText(jurnal.getTanggal().format(formatter));
        isiJurnalArea.setText(jurnal.getIsi());
        shareYes.setSelected(jurnal.isShared());
        shareNo.setSelected(!jurnal.isShared());
    }
    
    @FXML
    private void handleBuatJurnalBaru() {
        jurnalListView.getSelectionModel().clearSelection();
        jurnalYangDipilih = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
        tanggalDetailLabel.setText("Jurnal Baru - " + LocalDate.now().format(formatter));
        isiJurnalArea.clear();
        isiJurnalArea.requestFocus();
        shareNo.setSelected(true); 
    }

    @FXML
    private void handleSimpan() {
        String isi = isiJurnalArea.getText().trim();
        boolean isShared = shareYes.isSelected(); 

        if (isi.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Isi jurnal tidak boleh kosong.");
            return;
        }

        if (jurnalYangDipilih == null) { 
            LocalDate today = LocalDate.now();
            boolean sudahAdaJurnalHariIni = jurnalEntries.stream()
                .anyMatch(j -> j.getTanggal().isEqual(today));

            if (sudahAdaJurnalHariIni) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Anda sudah membuat entri jurnal untuk hari ini.");
                return;
            }
            
            Jurnal newJurnal = new Jurnal(currentUser.getUsername(), today, isi, isShared); // <<< PERBAIKAN DI SINI
            JurnalManager.addJurnal(newJurnal); 
            
        } else { 
            jurnalYangDipilih.setIsi(isi);
            jurnalYangDipilih.setShared(isShared); 
            JurnalManager.updateJurnal(jurnalYangDipilih);  
        }
        
        loadJurnalEntries(); 
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jurnal berhasil disimpan.");
    }

    @FXML
    private void handleHapus() {
        Jurnal jurnalUntukDihapus = jurnalListView.getSelectionModel().getSelectedItem();
        if (jurnalUntukDihapus == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih entri jurnal yang ingin dihapus.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus jurnal ini?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            JurnalManager.deleteJurnal(jurnalUntukDihapus); 
            loadJurnalEntries();
            handleBuatJurnalBaru();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jurnal telah dihapus.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
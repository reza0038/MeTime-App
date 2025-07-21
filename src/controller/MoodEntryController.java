package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import model.MoodEntry;
import model.User;
import model.MoodManager;
import model.UserSession;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class MoodEntryController implements Initializable {

    @FXML private ToggleGroup shareToggleGroup;
    @FXML private RadioButton shareYes; 
    @FXML private RadioButton shareNo;  
    @FXML private TextArea noteArea;
    @FXML private GridPane emojiGrid;
    @FXML private Button simpanButton;

    @FXML private TableView<MoodEntry> moodTableView;
    @FXML private TableColumn<MoodEntry, Date> dateColumn;
    @FXML private TableColumn<MoodEntry, String> moodColumn;
    @FXML private TableColumn<MoodEntry, String> noteColumn;
    @FXML private TableColumn<MoodEntry, Boolean> sharedColumn;

    private ObservableList<MoodEntry> moodEntries = FXCollections.observableArrayList();
    private ToggleGroup moodToggleGroup = new ToggleGroup();
    private MoodEntry selectedMoodEntry = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMoodButtons();
        setupTableColumns();
        loadMoodHistory();

        moodTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
    }

    private void populateForm(MoodEntry entry) {
        selectedMoodEntry = entry;

        for (Toggle toggle : moodToggleGroup.getToggles()) {
            if (toggle instanceof ToggleButton && entry.getMood().equals(((ToggleButton) toggle).getUserData())) {
                toggle.setSelected(true);
                break;
            }
        }
        noteArea.setText(entry.getNote());
        shareYes.setSelected(entry.isShared());
        shareNo.setSelected(!entry.isShared());
    }

    private void setupMoodButtons() {
        String[] moods = {"Senang", "Sedih", "Marah", "Cemas", "Bingung", "Netral"};
        emojiGrid.getChildren().clear();

        for (int i = 0; i < moods.length; i++) {
            String mood = moods[i];
            String imagePath = "/Image/" + mood.toLowerCase() + ".png";
            try {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(120);
                imageView.setFitWidth(120);
                ToggleButton button = new ToggleButton();
                button.setGraphic(imageView);
                button.setToggleGroup(moodToggleGroup);
                button.setUserData(mood);
                emojiGrid.add(button, i, 0);
            } catch (Exception e) {
                System.err.println("Gagal memuat gambar: " + imagePath);
                RadioButton rb = new RadioButton(mood);
                rb.setToggleGroup(moodToggleGroup);
                rb.setUserData(mood);
                emojiGrid.add(rb, i, 0);
            }
        }
    }
    
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        moodColumn.setCellValueFactory(new PropertyValueFactory<>("mood"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        sharedColumn.setCellValueFactory(new PropertyValueFactory<>("shared"));
    }

    private void loadMoodHistory() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            List<MoodEntry> userMoods = MoodManager.getMoodsByUsername(currentUser.getUsername());
            moodEntries.setAll(userMoods);
            moodTableView.setItems(moodEntries);
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        ToggleButton selectedToggle = (ToggleButton) moodToggleGroup.getSelectedToggle();
        if (selectedToggle == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Silakan pilih suasana hati Anda.");
            return;
        }

        String mood = (String) selectedToggle.getUserData();
        String note = noteArea.getText();
        boolean isShared = shareYes.isSelected();
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) return;

        if (selectedMoodEntry == null) { 
            MoodEntry newMoodEntry = new MoodEntry(currentUser.getUsername(), mood, note, isShared, new Date());
            MoodManager.addMood(newMoodEntry);
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data suasana hati baru berhasil disimpan!");
        
        } else { 
            selectedMoodEntry.setMood(mood);
            selectedMoodEntry.setNote(note);
            selectedMoodEntry.setShared(isShared);
            MoodManager.updateMood(selectedMoodEntry);
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Perubahan berhasil disimpan!");
        }

        loadMoodHistory();
        handleReset(null);
    }
    
    @FXML
    private void handleEdit(ActionEvent event) {
        MoodEntry entryToEdit = moodTableView.getSelectionModel().getSelectedItem();
        if (entryToEdit == null) {
            showAlert(Alert.AlertType.WARNING, "Pilih Item", "Silakan pilih item dari tabel yang ingin diedit.");
        }
    }
    
    @FXML
    private void handleDelete(ActionEvent event) {
        MoodEntry entryToDelete = moodTableView.getSelectionModel().getSelectedItem();
        if (entryToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan pilih entri dari tabel yang ingin dihapus.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, 
            "Anda yakin ingin menghapus entri mood ini?", 
            ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Konfirmasi Hapus");
        confirmation.setHeaderText(null);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                MoodManager.deleteMood(entryToDelete);
                loadMoodHistory();
                handleReset(null);
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Entri mood telah dihapus.");
            }
        });
    }

    @FXML
    private void handleReset(ActionEvent event) {
        selectedMoodEntry = null;
        moodTableView.getSelectionModel().clearSelection();
        if (moodToggleGroup.getSelectedToggle() != null) {
            moodToggleGroup.getSelectedToggle().setSelected(false);
        }
        noteArea.clear();
        shareNo.setSelected(true);
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
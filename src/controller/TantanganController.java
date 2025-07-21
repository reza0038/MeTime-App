package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration; 
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.AnyTypePermission;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import model.Tantangan; 
import model.TantanganProgress; 
import model.UserSession; 
import model.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType; 

public class TantanganController {

    // --- Elemen UI untuk Navigasi Panel ---
    @FXML private StackPane contentStackPane;
    @FXML private VBox listTantanganPane;
    @FXML private VBox detailTantanganPane;
    @FXML private VBox ongoingTantanganPane;
    @FXML private VBox confirmTantanganPane;
    @FXML private VBox historyTantanganPane;
    @FXML private VBox createTantanganPane;

    
    @FXML private ListView<Tantangan> tantanganListView; 
    @FXML private Label listMessageLabel;
    @FXML private Button buatTantanganBaruButton;

    // --- Elemen UI Detail Tantangan ---
    @FXML private Label detailJudulLabel;
    @FXML private Label detailDeskripsiLabel;
    @FXML private Label detailDurasiTargetLabel;
    @FXML private Label detailTargetRoleLabel;
    @FXML private Button mulaiTantanganButton;
    @FXML private Label detailMessageLabel;
    // Tombol Edit/Hapus Definisi
    @FXML private Button editDefinisiButton; // Tombol untuk edit definisi tantangan
    @FXML private Button hapusDefinisiButton; // Tombol untuk hapus definisi tantangan

    // --- Elemen UI Ongoing Tantangan ---
    @FXML private Label ongoingJudulLabel;
    @FXML private Label timerLabel;
    @FXML private Button selesaiTantanganButton;
    @FXML private Label ongoingMessageLabel;

    // --- Elemen UI Konfirmasi Tantangan ---
    @FXML private Label confirmJudulLabel;
    @FXML private Label confirmDurasiActualLabel;
    @FXML private TextArea feedbackTextArea;
    @FXML private RadioButton confirmShareYes;
    @FXML private RadioButton confirmShareNo;
    @FXML private Button konfirmasiSelesaiButton;
    @FXML private Button batalkanKonfirmasiButton;
    @FXML private Label confirmMessageLabel;

    // --- Elemen UI Riwayat Tantangan ---
    @FXML private TableView<TantanganProgress> tantanganHistoryTable;
    @FXML private TableColumn<TantanganProgress, Tantangan> historyNamaTantanganCol;
    @FXML private TableColumn<TantanganProgress, String> historyNamaRemajaCol;
    @FXML private TableColumn<TantanganProgress, LocalDateTime> historyWaktuMulaiCol;
    @FXML private TableColumn<TantanganProgress, LocalDateTime> historyWaktuSelesaiCol;
    @FXML private TableColumn<TantanganProgress, String> historyDurasiCol;
    @FXML private TableColumn<TantanganProgress, Boolean> historySelesaiCol;
    @FXML private TableColumn<TantanganProgress, Boolean> historySharedCol;
    @FXML private Button kembaliKeDaftarButton;
    // Tombol Edit dan Hapus progres (ada di FXML History Pane)
    @FXML private Button editProgresButton; // Asumsi ada fx:id ini di FXML
    @FXML private Button hapusProgresButton; // Asumsi ada fx:id ini di FXML

    // --- Elemen UI Buat Tantangan Baru ---
    @FXML private Label createEditTantanganTitle; // Untuk judul dinamis "Buat" atau "Edit" Tantangan
    @FXML private TextField newTantanganJudulField;
    @FXML private TextArea newTantanganDeskripsiArea;
    @FXML private TextField newTantanganDurasiMenitField;
    @FXML private ComboBox<String> newTantanganTargetRoleCombo;
    @FXML private Button simpanTantanganBaruButton; // Ini akan menjadi tombol Save Definition
    @FXML private Button batalTantanganBaruButton;
    @FXML private Label newTantanganMessageLabel;


    
    private ObservableList<Tantangan> predefinedTantangan = FXCollections.observableArrayList();
    private ObservableList<Tantangan> customTantangan = FXCollections.observableArrayList();
    private ObservableList<TantanganProgress> tantanganProgressEntries = FXCollections.observableArrayList();
    private Tantangan selectedTantanganDefinition = null; // Definisi tantangan yang sedang dilihat/diedit/dipilih
    private TantanganProgress currentOngoingTantangan = null; // Progres tantangan yang sedang berjalan

    private Tantangan editingCustomTantangan = null; // Untuk melacak tantangan kustom yang sedang dalam mode edit

    private Timeline timeline;
    private LocalDateTime timerStartTime;

    private static final String TANTANGAN_PROGRESS_FILE = "xml/tantangan_progress.xml";
    private static final String TANTANGAN_DEFINITIONS_FILE = "xml/tantangan_definitions.xml"; 

    private String currentUser;
    private String currentUserRole;

    @FXML
    public void initialize() {
        User loggedInUser = UserSession.getCurrentUser();
        if (loggedInUser != null) {
            currentUser = loggedInUser.getUsername();
            currentUserRole = loggedInUser.getPeran();
        } else {
            currentUser = "Tamu";
            currentUserRole = "Remaja";
            System.err.println("Warning: TantanganController initialized without logged-in user.");
        }

        predefinedTantangan.addAll(
            new Tantangan("Olahraga 30 menit", "Melakukan olahraga minimal 30 menit. Lebih seru jika dilakukan bersama-sama.", java.time.Duration.ofMinutes(30), "Semua"),
            new Tantangan("Membaca Buku 20 Menit", "Membaca buku fisik atau digital minimal 20 menit.", java.time.Duration.ofMinutes(20), "Remaja"),
            new Tantangan("Meditasi 10 Menit", "Melakukan meditasi terpandu atau hening selama 10 menit. Bisa mengikuti tutorial maupun meditasi sendiri.", java.time.Duration.ofMinutes(10), "Semua"),
            new Tantangan("Menulis Jurnal", "Menulis di jurnal harian minimal 15 menit.", java.time.Duration.ofMinutes(15), "Remaja"),
            new Tantangan("Minum Air 1 Liter", "Memastikan asupan air 1 liter dalam satu hari.", java.time.Duration.ofDays(0), "Semua"),
            new Tantangan("Berjalan Kaki 15 Menit", "Berjalan kaki santai selama 15 menit.", java.time.Duration.ofMinutes(15), "Semua"),
            new Tantangan("Belajar Bahasa Asing 30 Menit", "Belajar kosakata atau grammar bahasa asing selama 30 menit. Lebih baik jika dilakukan bersama.", java.time.Duration.ofMinutes(30), "Remaja"),
            new Tantangan("Membereskan Ruangan Pribadi", "Merapikan dan membersihkan ruang pribadi selama 30 menit. Bisa dilakukan bersama-sama.", java.time.Duration.ofMinutes(30), "Remaja"),
            new Tantangan("Anti-Perangkat Digital 1 Jam", "Tidak menggunakan perangkat digital selama 1 jam, seperti Handphone, IPad, Laptop dan lain-lain.", java.time.Duration.ofHours(1), "Remaja")
        );
        
        newTantanganTargetRoleCombo.getItems().addAll("Remaja", "Orang Tua", "Semua");
        newTantanganTargetRoleCombo.setValue("Remaja");

        // --- Konfigurasi TableView Riwayat ---
        historyNamaTantanganCol.setCellValueFactory(new PropertyValueFactory<>("tantangan"));
        historyNamaTantanganCol.setCellFactory(column -> new TableCell<TantanganProgress, Tantangan>() {
            @Override
            protected void updateItem(Tantangan item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getJudul());
            }
        });
        historyNamaRemajaCol.setCellValueFactory(new PropertyValueFactory<>("namaRemaja"));
        historyWaktuMulaiCol.setCellValueFactory(new PropertyValueFactory<>("waktuMulaiAktual"));
        historyWaktuSelesaiCol.setCellValueFactory(new PropertyValueFactory<>("waktuSelesaiAktual"));
        historyDurasiCol.setCellValueFactory(new PropertyValueFactory<>("durasiPengerjaanFormatted"));
        historySelesaiCol.setCellValueFactory(new PropertyValueFactory<>("selesai"));
        historySharedCol.setCellValueFactory(new PropertyValueFactory<>("shared"));

        tantanganHistoryTable.setItems(tantanganProgressEntries);

        // --- Grup Toggle untuk RadioButton Konfirmasi Share ---
        ToggleGroup confirmShareGroup = new ToggleGroup();
        confirmShareYes.setToggleGroup(confirmShareGroup);
        confirmShareNo.setToggleGroup(confirmShareGroup);
        confirmShareNo.setSelected(true);

        loadTantanganDefinitions();
        loadTantanganProgressEntries();

        tantanganListView.setCellFactory(lv -> new ListCell<Tantangan>() {
            @Override
            protected void updateItem(Tantangan item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label titleLabel = new Label(item.getJudul());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3F51B5;");

                    String deskripsiPreview = item.getDeskripsi().length() > 50 ? item.getDeskripsi().substring(0, 50) + "..." : item.getDeskripsi();
                    Label descLabel = new Label(deskripsiPreview);
                    descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #607D8B;");

                    Label roleLabel = new Label("Target: " + item.getTargetRole());
                    roleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9E9E9E;");

                    VBox cellContent = new VBox(5, titleLabel, descLabel, roleLabel);
                    cellContent.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));

                    setGraphic(cellContent);
                    getStyleClass().add("tantangan-list-cell");
                }
            }
        });

        updateTantanganListViewContent();
        showPanel(listTantanganPane);
    }

    private void showPanel(VBox panelToShow) {
        listTantanganPane.setVisible(false);
        detailTantanganPane.setVisible(false);
        ongoingTantanganPane.setVisible(false);
        confirmTantanganPane.setVisible(false);
        historyTantanganPane.setVisible(false);
        createTantanganPane.setVisible(false);

        panelToShow.setVisible(true);
        StackPane.setAlignment(panelToShow, javafx.geometry.Pos.CENTER);
        StackPane.setMargin(panelToShow, new javafx.geometry.Insets(0));
    }

    private XStream createProgressXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{
            TantanganProgress.class, ArrayList.class, LocalDateTime.class, LocalDate.class,
            Boolean.class, String.class, Tantangan.class, java.time.Duration.class
        });
        xstream.alias("tantanganProgressEntry", TantanganProgress.class);
        xstream.alias("tantanganProgressHistory", List.class);
        xstream.alias("tantanganDefinisi", Tantangan.class);
        return xstream;
    }

    private void loadTantanganProgressEntries() {
        XStream xstream = createProgressXStream();
        try (FileReader reader = new FileReader(TANTANGAN_PROGRESS_FILE)) {
            Object readObject = xstream.fromXML(reader);
            if (readObject instanceof List) {
                List<?> rawList = (List<?>) readObject;
                tantanganProgressEntries.clear();
                for (Object item : rawList) {
                    if (item instanceof TantanganProgress) {
                        tantanganProgressEntries.add((TantanganProgress) item);
                    }
                }
                tantanganProgressEntries.sort(Comparator.comparing(TantanganProgress::getWaktuMulaiAktual).reversed());
            } else {
                tantanganProgressEntries.clear();
            }
        } catch (IOException e) {
            tantanganProgressEntries.clear();
            System.out.println("No existing tantangan progress history file found: " + e.getMessage());
        } catch (Exception e) {
            tantanganProgressEntries.clear();
            System.err.println("Error deserializing tantangan progress history: " + e.getMessage());
            showAlert("Error", "Gagal memuat riwayat progres tantangan. Berkas mungkin rusak.");
        }
    }

    private void saveTantanganProgressEntries() {
        XStream xstream = createProgressXStream();
        try (FileWriter writer = new FileWriter(TANTANGAN_PROGRESS_FILE)) {
            xstream.toXML(new ArrayList<>(tantanganProgressEntries), writer);
        } catch (IOException e) {
            showAlert("Error", "Gagal menyimpan progres tantangan: " + e.getMessage());
        }
    }

    private XStream createDefinitionsXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{
            Tantangan.class, ArrayList.class, java.time.Duration.class, String.class
        });
        xstream.alias("tantanganDefinisi", Tantangan.class);
        xstream.alias("daftarTantanganKustom", List.class);
        return xstream;
    }

    private void loadTantanganDefinitions() {
        XStream xstream = createDefinitionsXStream();
        try (FileReader reader = new FileReader(TANTANGAN_DEFINITIONS_FILE)) {
            Object readObject = xstream.fromXML(reader);
            if (readObject instanceof List) {
                List<?> rawList = (List<?>) readObject;
                customTantangan.clear();
                for (Object item : rawList) {
                    if (item instanceof Tantangan) {
                        customTantangan.add((Tantangan) item);
                    }
                }
            } else {
                customTantangan.clear();
            }
        } catch (IOException e) {
            customTantangan.clear();
            System.out.println("No existing custom tantangan definitions file found: " + e.getMessage());
        } catch (Exception e) {
            customTantangan.clear();
            System.err.println("Error deserializing custom tantangan definitions: " + e.getMessage());
            showAlert("Error", "Gagal memuat definisi tantangan kustom. Berkas mungkin rusak.");
        }
    }

    private void saveTantanganDefinitions() {
        XStream xstream = createDefinitionsXStream();
        try (FileWriter writer = new FileWriter(TANTANGAN_DEFINITIONS_FILE)) {
            xstream.toXML(new ArrayList<>(customTantangan), writer);
        } catch (IOException e) {
            showAlert("Error", "Gagal menyimpan definisi tantangan kustom: " + e.getMessage());
        }
    }

    // --- Helper untuk memperbarui konten ListView tantangan yang relevan ---
    private void updateTantanganListViewContent() {
        tantanganListView.getSelectionModel().clearSelection();
        tantanganListView.getItems().clear();
        
        List<Tantangan> allAvailableTantangan = new ArrayList<>(predefinedTantangan);
        allAvailableTantangan.addAll(customTantangan);

        List<Tantangan> filteredTantangan = allAvailableTantangan.stream()
            .filter(t -> t.getTargetRole().equals("Semua") || t.getTargetRole().equals(currentUserRole))
            .collect(Collectors.toList());

        filteredTantangan.sort(Comparator.comparing(Tantangan::getJudul));

        tantanganListView.setItems(FXCollections.observableArrayList(filteredTantangan));

        if (tantanganListView.getItems().isEmpty()) {
            listMessageLabel.setText("Tidak ada tantangan tersedia untuk peran Anda. Buat yang baru!");
        } else {
            listMessageLabel.setText("Pilih tantangan di bawah ini:");
        }
    }


    // --- Aksi-aksi UI ---

    // Dipanggil saat item di ListView dipilih (Langkah 3)
    @FXML
    private void handleSelectTantangan() {
        selectedTantanganDefinition = tantanganListView.getSelectionModel().getSelectedItem();

        if (selectedTantanganDefinition != null) {
            detailJudulLabel.setText(selectedTantanganDefinition.getJudul());
            detailDeskripsiLabel.setText(selectedTantanganDefinition.getDeskripsi());
            detailDurasiTargetLabel.setText(selectedTantanganDefinition.getDurasiTargetFormatted());
            detailTargetRoleLabel.setText("Target: " + selectedTantanganDefinition.getTargetRole());
            showPanel(detailTantanganPane);
            detailMessageLabel.setText("Baca detail tantangan dan klik 'Mulai'.");
        } else {
            detailMessageLabel.setText("Tidak ada tantangan yang dipilih.");
        }
    }

    // Dipanggil saat tombol "Mulai Tantangan" diklik (Langkah 6 & 7)
    @FXML
    private void handleMulaiTantangan() {
        if (selectedTantanganDefinition == null) {
            detailMessageLabel.setText("Silakan pilih tantangan terlebih dahulu.");
            return;
        }

        currentOngoingTantangan = new TantanganProgress(
            selectedTantanganDefinition,
            currentUser,
            LocalDateTime.now(),
            false, // Belum selesai
            "",    // Feedback kosong
            false  // Belum shared
        );

        ongoingJudulLabel.setText(selectedTantanganDefinition.getJudul());
        timerStartTime = LocalDateTime.now();
        startTimer();
        showPanel(ongoingTantanganPane);
        ongoingMessageLabel.setText("Tantangan dimulai! Fokus pada tujuanmu.");
    }

    // Memulai/melanjutkan timer (Langkah 8)
    private void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> {
            long secondsElapsed = ChronoUnit.SECONDS.between(timerStartTime, LocalDateTime.now());
            timerLabel.setText(formatTime(secondsElapsed));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    // Memformat detik menjadi HH:MM:SS
    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Dipanggil saat tombol "Selesai Tantangan" diklik (Langkah 9 & 10)
    @FXML
    private void handleSelesaiTantangan() {
        if (timeline != null) {
            timeline.stop();
        }
        if (currentOngoingTantangan != null) {
            currentOngoingTantangan.setWaktuSelesaiAktual(LocalDateTime.now());

            confirmJudulLabel.setText(currentOngoingTantangan.getTantangan().getJudul());
            confirmDurasiActualLabel.setText(currentOngoingTantangan.getDurasiPengerjaanFormatted());
            feedbackTextArea.clear();
            confirmShareNo.setSelected(true);

            showPanel(confirmTantanganPane);
            confirmMessageLabel.setText("Konfirmasi penyelesaian tantangan. Beri feedback dan pilih opsi berbagi.");
        } else {
            ongoingMessageLabel.setText("Tidak ada tantangan yang sedang berjalan.");
        }
    }

    // Dipanggil saat tombol "Konfirmasi Selesai" diklik (Langkah 11 & 12)
   // Dalam TantanganController.java

// ... (kode sebelumnya) ...

    // Dipanggil saat tombol "Konfirmasi Selesai" diklik (Langkah 11 & 12)
    @FXML
    private void handleKonfirmasiSelesai() {
        if (currentOngoingTantangan != null) {
            currentOngoingTantangan.setSelesai(true);
            currentOngoingTantangan.setFeedbackPengguna(feedbackTextArea.getText().trim());
            currentOngoingTantangan.setShared(confirmShareYes.isSelected());

            tantanganProgressEntries.add(currentOngoingTantangan);
            tantanganProgressEntries.sort(Comparator.comparing(TantanganProgress::getWaktuMulaiAktual).reversed());
            saveTantanganProgressEntries();

            showAlert("Tantangan Berhasil Diselesaikan!", "Selamat! Anda hebat telah menyelesaikan tantangan ini. Teruslah berprogress!");
            currentOngoingTantangan = null;
            resetForm();
            showPanel(listTantanganPane);
            updateTantanganListViewContent();
            loadTantanganProgressEntries();
        }
    }

    // Dipanggil saat tombol "Batalkan Konfirmasi" diklik
    @FXML
    private void handleBatalkanKonfirmasi() {
        showAlert("Konfirmasi Dibatalkan", "Tantangan dibatalkan. Progress tidak disimpan.");
        currentOngoingTantangan = null;
        resetForm();
        showPanel(listTantanganPane);
        updateTantanganListViewContent();
    }
    
    // Dipanggil saat tombol "Kembali ke Daftar Tantangan" di panel detail/history/create diklik
    @FXML
    private void handleKembaliKeDaftar() {
        resetForm();
        showPanel(listTantanganPane);
        updateTantanganListViewContent();
    }

    // Dipanggil saat tombol "Lihat Riwayat Tantangan" di panel daftar diklik
    @FXML
    private void handleLihatRiwayat() {
        loadTantanganProgressEntries();
        showPanel(historyTantanganPane);
    }

    // Dipanggil saat tombol "Buat Tantangan Baru" diklik
    @FXML
    private void handleBuatTantanganBaru() {
        resetForm();
        newTantanganTargetRoleCombo.setValue(currentUserRole);
        showPanel(createTantanganPane);
        createEditTantanganTitle.setText("Buat Tantangan Baru");
    }

    // Dipanggil saat tombol "Simpan Tantangan Baru" diklik (handleSaveTantanganDefinition)
    @FXML
    private void handleSimpanTantanganBaru() {
        String judul = newTantanganJudulField.getText().trim();
        String deskripsi = newTantanganDeskripsiArea.getText().trim();
        String durasiMenitText = newTantanganDurasiMenitField.getText().trim();
        String targetRole = newTantanganTargetRoleCombo.getValue();

        if (judul.isEmpty() || deskripsi.isEmpty() || durasiMenitText.isEmpty() || targetRole == null) {
            newTantanganMessageLabel.setText("Semua field harus diisi.");
            return;
        }

        try {
            long durasiMenit = Long.parseLong(durasiMenitText);
            java.time.Duration durasi = java.time.Duration.ofMinutes(durasiMenit);

            boolean isDuplicate = (editingCustomTantangan == null || !editingCustomTantangan.getJudul().equals(judul)) &&
                                  (predefinedTantangan.stream().anyMatch(t -> t.getJudul().equals(judul)) ||
                                   customTantangan.stream().anyMatch(t -> t.getJudul().equals(judul)));
            if (isDuplicate) {
                newTantanganMessageLabel.setText("Judul tantangan sudah ada.");
                return;
            }

            if (editingCustomTantangan == null) {
                Tantangan newCustomTantangan = new Tantangan(judul, deskripsi, durasi, targetRole);
                customTantangan.add(newCustomTantangan);
                newTantanganMessageLabel.setText("Tantangan baru berhasil dibuat!");
                showAlert("Berhasil", "Tantangan '" + judul + "' berhasil ditambahkan.");
            } else {
                editingCustomTantangan.setJudul(judul);
                editingCustomTantangan.setDeskripsi(deskripsi);
                editingCustomTantangan.setDurasiTarget(durasi);
                editingCustomTantangan.setTargetRole(targetRole);
                newTantanganMessageLabel.setText("Tantangan berhasil diperbarui!");
                showAlert("Berhasil", "Tantangan '" + judul + "' berhasil diperbarui.");
            }
            saveTantanganDefinitions();
            
            resetForm();
            showPanel(listTantanganPane);
            updateTantanganListViewContent();
            editingCustomTantangan = null;
        } catch (NumberFormatException e) {
            newTantanganMessageLabel.setText("Durasi harus angka.");
        }
    }


    // Metode internal untuk mereset formulir dan state
    private void resetForm() {
        detailMessageLabel.setText("");
        ongoingMessageLabel.setText("");
        confirmMessageLabel.setText("");
        feedbackTextArea.clear();
        confirmShareNo.setSelected(true);
        selectedTantanganDefinition = null;
        currentOngoingTantangan = null;
        editingCustomTantangan = null;
        if (timeline != null) {
            timeline.stop();
            timerLabel.setText("00:00:00");
        }
        tantanganHistoryTable.getSelectionModel().clearSelection();
        tantanganListView.getSelectionModel().clearSelection();

        newTantanganJudulField.clear();
        newTantanganDeskripsiArea.clear();
        newTantanganDurasiMenitField.clear();
        newTantanganTargetRoleCombo.setValue("Remaja");
        newTantanganMessageLabel.setText("");

        if (createEditTantanganTitle != null) {
            createEditTantanganTitle.setText("Buat Tantangan Baru");
        }
    }

    // Metode untuk menampilkan Alert box yang konsisten
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Metode handleEdit (Edit Progres) ---
    @FXML
    private void handleEdit() { // Ini adalah handleEdit untuk mengedit progres tantangan yang sudah selesai/berjalan
        TantanganProgress selectedEntry = tantanganHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            showAlert("Peringatan", "Pilih entri progres tantangan dari riwayat untuk diedit.");
            return;
        }

        currentOngoingTantangan = selectedEntry;
        confirmJudulLabel.setText(selectedEntry.getTantangan().getJudul());
        confirmDurasiActualLabel.setText(selectedEntry.getDurasiPengerjaanFormatted());
        feedbackTextArea.setText(selectedEntry.getFeedbackPengguna());
        confirmShareYes.setSelected(selectedEntry.isShared());
        confirmShareNo.setSelected(!selectedEntry.isShared());
        
        showPanel(confirmTantanganPane);
        confirmMessageLabel.setText("Edit progres tantangan. Lakukan perubahan dan klik 'Konfirmasi Selesai'.");
    }

    // --- Metode handleDelete (Hapus Progres) ---
    @FXML
    private void handleDelete() {
        TantanganProgress entryToDelete = tantanganHistoryTable.getSelectionModel().getSelectedItem();
        if (entryToDelete == null) {
            showAlert("Peringatan", "Pilih tantangan dari riwayat untuk dihapus.");
            return;
        }

        Alert confirmation = new Alert(AlertType.CONFIRMATION); // <<< PERBAIKAN DI SINI
        confirmation.setTitle("Konfirmasi Hapus"); // <<< PERBAIKAN DI SINI
        confirmation.setHeaderText(null);
        confirmation.setContentText("Yakin ingin menghapus progres tantangan ini?"); // <<< PERBAIKAN DI SINI
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            tantanganProgressEntries.remove(entryToDelete);
            saveTantanganProgressEntries();
            
            showAlert("Sukses", "Progres tantangan telah dihapus dari riwayat.");
            resetForm();
            loadTantanganProgressEntries();
        }
    }

    // --- Metode handleEditDefinition (Edit Definisi) ---
    // Pastikan tombol ini ada di tantangan.fxml dan onAction-nya mengarah ke sini
    @FXML
    private void handleEditDefinition() {
        // selectedTantanganDefinition sudah diset dari handleSelectTantangan() atau ListView
        if (selectedTantanganDefinition == null) {
            showAlert("Peringatan", "Pilih tantangan dari daftar untuk diedit definisinya.");
            return;
        }

        // Cek apakah ini tantangan bawaan (predefined)
        if (predefinedTantangan.contains(selectedTantanganDefinition)) {
            showAlert("Peringatan", "Tantangan bawaan tidak dapat diedit.");
            return;
        }

        // Ini adalah tantangan kustom yang akan diedit
        editingCustomTantangan = selectedTantanganDefinition;

        // Isi form pembuatan tantangan dengan data tantangan yang akan diedit
        newTantanganJudulField.setText(editingCustomTantangan.getJudul());
        newTantanganDeskripsiArea.setText(editingCustomTantangan.getDeskripsi());
        newTantanganDurasiMenitField.setText(String.valueOf(editingCustomTantangan.getDurasiTarget().toMinutes()));
        newTantanganTargetRoleCombo.setValue(editingCustomTantangan.getTargetRole());

        createEditTantanganTitle.setText("Edit Tantangan"); // Ubah judul panel
        showPanel(createTantanganPane); 
        newTantanganMessageLabel.setText("Lakukan perubahan dan klik 'Simpan Tantangan Baru'.");
    }

    
    @FXML
    private void handleDeleteDefinition() {
        if (selectedTantanganDefinition == null) {
            showAlert("Peringatan", "Pilih tantangan dari daftar untuk dihapus definisinya.");
            return;
        }

        if (predefinedTantangan.contains(selectedTantanganDefinition)) {
            showAlert("Peringatan", "Tantangan bawaan tidak dapat dihapus.");
            return;
        }

        Alert confirmation = new Alert(AlertType.CONFIRMATION); 
        confirmation.setTitle("Konfirmasi Hapus Definisi"); 
        confirmation.setHeaderText(null);
        confirmation.setContentText("Yakin ingin menghapus definisi tantangan '" + selectedTantanganDefinition.getJudul() + "'?"); 
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            customTantangan.remove(selectedTantanganDefinition); 
            saveTantanganDefinitions(); 
            showAlert("Sukses", "Tantangan '" + selectedTantanganDefinition.getJudul() + "' berhasil dihapus.");
            resetForm(); 
            showPanel(listTantanganPane); 
            updateTantanganListViewContent(); 
 }
}
}
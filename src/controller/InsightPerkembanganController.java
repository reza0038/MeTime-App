package controller;

import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.*;
import model.MoodManager;
import model.JurnalManager;
import model.TantanganManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class InsightPerkembanganController implements Initializable {

    
    @FXML private ComboBox<String> timeRangeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label insightMessageLabel;

    
    
    @FXML private TableView<MoodEntry> moodTableView;
    @FXML private TableColumn<MoodEntry, Date> moodDateColumn;
    @FXML private TableColumn<MoodEntry, String> moodTypeColumn;
    @FXML private TableColumn<MoodEntry, String> moodNoteColumn;
    @FXML private TableColumn<MoodEntry, Boolean> moodSharedColumn;

 
    @FXML private TableView<Jurnal> jurnalTableView;
    @FXML private TableColumn<Jurnal, LocalDate> jurnalDateColumn;
    @FXML private TableColumn<Jurnal, String> jurnalContentColumn;
    @FXML private TableColumn<Jurnal, Boolean> jurnalSharedColumn;


    @FXML private TableView<TantanganProgress> tantanganTableView;
    @FXML private TableColumn<TantanganProgress, String> tantanganNamaColumn;
    @FXML private TableColumn<TantanganProgress, LocalDateTime> tantanganMulaiColumn;
    @FXML private TableColumn<TantanganProgress, LocalDateTime> tantanganSelesaiColumn;
    @FXML private TableColumn<TantanganProgress, String> tantanganDurasiColumn;
    @FXML private TableColumn<TantanganProgress, Boolean> tantanganStatusColumn;
    @FXML private TableColumn<TantanganProgress, Boolean> tantanganSharedColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        timeRangeComboBox.getItems().addAll("Minggu Terakhir", "Bulan Ini", "3 Bulan Terakhir", "Semua Waktu", "Rentang Kustom");
        timeRangeComboBox.setValue("Semua Waktu");
        timeRangeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadAndDisplayInsight());
        
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.disableProperty().bind(timeRangeComboBox.valueProperty().isNotEqualTo("Rentang Kustom"));
        endDatePicker.disableProperty().bind(timeRangeComboBox.valueProperty().isNotEqualTo("Rentang Kustom"));
        startDatePicker.setOnAction(event -> loadAndDisplayInsight());
        endDatePicker.setOnAction(event -> loadAndDisplayInsight());

        setupTableColumns();
        loadAndDisplayInsight();
    }

    
    private void setupTableColumns() {
       
        moodDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        moodTypeColumn.setCellValueFactory(new PropertyValueFactory<>("mood"));
        moodNoteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        moodSharedColumn.setCellValueFactory(new PropertyValueFactory<>("shared"));

      
        jurnalDateColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        jurnalContentColumn.setCellValueFactory(new PropertyValueFactory<>("isi"));
        jurnalSharedColumn.setCellValueFactory(new PropertyValueFactory<>("shared"));

     
        tantanganNamaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTantangan().getJudul()));
        tantanganMulaiColumn.setCellValueFactory(new PropertyValueFactory<>("waktuMulaiAktual"));
        tantanganSelesaiColumn.setCellValueFactory(new PropertyValueFactory<>("waktuSelesaiAktual"));
        tantanganDurasiColumn.setCellValueFactory(new PropertyValueFactory<>("durasiPengerjaanFormatted"));
        tantanganStatusColumn.setCellValueFactory(new PropertyValueFactory<>("selesai"));
        tantanganSharedColumn.setCellValueFactory(new PropertyValueFactory<>("shared"));
    }

    @FXML
    private void loadAndDisplayInsight() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        
        List<MoodEntry> allMoods = MoodManager.getAllMoods();
        List<Jurnal> allJurnals = JurnalManager.getAllJurnals();
        List<TantanganProgress> allTantangan = TantanganManager.getAllTantanganProgress();

        
        List<MoodEntry> filteredMoods = allMoods.stream()
                .filter(entry -> entry.isShared() && isWithinDateRange(entry.getDate(), startDate, endDate))
                .collect(Collectors.toList());

        List<Jurnal> filteredJurnals = allJurnals.stream()
                .filter(entry -> entry.isShared() && isWithinDateRange(entry.getTanggal(), startDate, endDate))
                .collect(Collectors.toList());
        
        List<TantanganProgress> filteredTantangan = allTantangan.stream()
                .filter(entry -> entry.isShared() && isWithinDateRange(entry.getWaktuMulaiAktual().toLocalDate(), startDate, endDate))
                .collect(Collectors.toList());

   
        moodTableView.setItems(FXCollections.observableArrayList(filteredMoods));
        jurnalTableView.setItems(FXCollections.observableArrayList(filteredJurnals));
        tantanganTableView.setItems(FXCollections.observableArrayList(filteredTantangan));

        insightMessageLabel.setText(
            String.format("Menampilkan insight dari %d mood, %d jurnal, dan %d tantangan (shared).",
            filteredMoods.size(), filteredJurnals.size(), filteredTantangan.size())
        );
    }
    

    private boolean isWithinDateRange(Date date, LocalDate start, LocalDate end) {
        if (date == null) return false;
        LocalDate entryDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return isWithinDateRange(entryDate, start, end);
    }

    private boolean isWithinDateRange(LocalDate entryDate, LocalDate start, LocalDate end) {
        if (entryDate == null) return false;
        boolean afterStart = (start == null || !entryDate.isBefore(start));
        boolean beforeEnd = (end == null || !entryDate.isAfter(end));
        return afterStart && beforeEnd;
    }
}
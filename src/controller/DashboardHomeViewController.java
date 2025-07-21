package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import model.UserSession;
import model.User;
import model.MoodEntry;
import model.Jurnal;
import model.MoodManager;   
import model.JurnalManager; 

public class DashboardHomeViewController implements Initializable {

    
    @FXML private StackedAreaChart<String, Number> moodDevelopmentAreaChart;
    @FXML private PieChart moodCompositionChart;
    @FXML private BarChart<String, Number> activityFrequencyChart;
    @FXML private Label sapaanLabel;
    @FXML private Label pesanMotivasiLabel;
    @FXML private Label tantanganLabel;
    @FXML private Label saranLabel;
    @FXML private Label lastMoodLabel;
    @FXML private Label lastMoodTimeLabel;
    @FXML private Label totalMoodsLabel;
    @FXML private Label totalJurnalsLabel;

 
    private User currentUser;
    private List<MoodEntry> userMoodHistory;
    private List<Jurnal> userJurnalHistory;

  
    private DashboardController mainController;

    
    public void setMainController(DashboardController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            if (sapaanLabel != null) sapaanLabel.setText("Pengguna tidak ditemukan.");
            return;
        }
        loadUserData();
        setupInfoCards();
        setupCharts();
    }
    
    
    private void loadUserData() {
        if (currentUser != null) {
            this.userMoodHistory = MoodManager.getMoodsByUsername(currentUser.getUsername());
            this.userJurnalHistory = JurnalManager.getJurnalsByUsername(currentUser.getUsername());
        } else {
            this.userMoodHistory = new ArrayList<>();
            this.userJurnalHistory = new ArrayList<>();
        }
    }
    private void setupInfoCards() {
        sapaanLabel.setText("Halo, " + currentUser.getUsername() + "!");
        pesanMotivasiLabel.setText("Semoga harimu menyenangkan dan penuh makna.");
        List<String> saranList = List.of(
            "Jangan lupa minum air yang cukup hari ini untuk menjaga mood dan fokusmu.",
            "Mengambil jeda singkat setiap jam dapat membantu menyegarkan pikiran.",
            "Mendengarkan musik favoritmu bisa menjadi cara cepat untuk memperbaiki suasana hati."
        );
        saranLabel.setText(saranList.get(new Random().nextInt(saranList.size())));
        tantanganLabel.setText("Tantangan: Membaca buku minimal 10 halaman.");
        setupActivitySummary();
    }
    private void setupActivitySummary() {
        int totalMoods = (userMoodHistory != null) ? userMoodHistory.size() : 0;
        int totalJurnals = (userJurnalHistory != null) ? userJurnalHistory.size() : 0;
        totalMoodsLabel.setText("Total Entri Mood: " + totalMoods);
        totalJurnalsLabel.setText("Total Entri Jurnal: " + totalJurnals);

        if (totalMoods > 0) {
            userMoodHistory.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparing(MoodEntry::getDate))
                .ifPresent(lastMood -> {
                    lastMoodLabel.setText("Mood Terakhir: " + lastMood.getMood());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
                    lastMoodTimeLabel.setText("Pada: " + dateFormat.format(lastMood.getDate()));
                });
        } else {
            lastMoodLabel.setText("Mood Terakhir: -");
            lastMoodTimeLabel.setText("Belum ada entri mood.");
        }
    }
    private void setupCharts() {
        setupMoodDevelopmentAreaChart();
        setupMoodCompositionChart();
        setupActivityFrequencyChart();
    }
    private void setupMoodDevelopmentAreaChart() {
        moodDevelopmentAreaChart.getData().clear();
        Set<String> positifMoods = Set.of("Senang", "Netral");
        Map<LocalDate, Map<String, Long>> weeklyData = new LinkedHashMap<>();
        for (int i = 3; i >= 0; i--) {
            LocalDate weekStartDate = LocalDate.now().minusWeeks(i).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            weeklyData.put(weekStartDate, new HashMap<>(Map.of("Positif", 0L, "Negatif", 0L)));
        }

        if (userMoodHistory != null) {
            userMoodHistory.stream()
                .filter(entry -> entry != null && entry.getDate() != null && entry.getMood() != null)
                .forEach(entry -> {
                    LocalDate entryDate = entry.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate weekStartDate = entryDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    weeklyData.computeIfPresent(weekStartDate, (key, counts) -> {
                        counts.merge(positifMoods.contains(entry.getMood()) ? "Positif" : "Negatif", 1L, Long::sum);
                        return counts;
                    });
                });
        }
        
        XYChart.Series<String, Number> positiveSeries = new XYChart.Series<>();
        positiveSeries.setName("Positif");
        XYChart.Series<String, Number> negativeSeries = new XYChart.Series<>();
        negativeSeries.setName("Negatif");

        DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("'Minggu' w");
        weeklyData.forEach((weekStart, counts) -> {
            String weekLabel = weekStart.format(weekFormatter);
            positiveSeries.getData().add(new XYChart.Data<>(weekLabel, counts.get("Positif")));
            negativeSeries.getData().add(new XYChart.Data<>(weekLabel, counts.get("Negatif")));
        });
        
        moodDevelopmentAreaChart.getData().setAll(negativeSeries, positiveSeries);
    }
    private void setupMoodCompositionChart() {
        moodCompositionChart.getData().clear();
        if (userMoodHistory == null || userMoodHistory.isEmpty()) {
            moodCompositionChart.setData(FXCollections.observableArrayList(new PieChart.Data("Belum ada data", 1)));
            return;
        }
        Map<String, Long> moodCounts = userMoodHistory.stream()
                .filter(entry -> entry != null && entry.getMood() != null)
                .collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.counting()));
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        moodCounts.forEach((mood, count) -> pieChartData.add(new PieChart.Data(mood, count)));
        moodCompositionChart.setData(pieChartData);
    }
    private void setupActivityFrequencyChart() {
        activityFrequencyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Aktivitas");

        if (userMoodHistory != null) {
            Map<DayOfWeek, Long> moodByDay = userMoodHistory.stream()
                .filter(e -> e != null && e.getDate() != null)
                .collect(Collectors.groupingBy(e -> e.getDate().toInstant().atZone(ZoneId.systemDefault()).getDayOfWeek(), Collectors.counting()));
            
            Locale locale = new Locale("id", "ID");
            for (DayOfWeek day : DayOfWeek.values()) {
                String namaHari = day.getDisplayName(TextStyle.SHORT, locale);
                series.getData().add(new XYChart.Data<>(namaHari, moodByDay.getOrDefault(day, 0L)));
            }
        }
        
        activityFrequencyChart.getData().setAll(series);
    }



    @FXML
    private void handleBukaMoodTracker() {
        if (mainController != null) {
            mainController.showMoodTracker();
        } else {
            System.err.println("MainController is null, cannot navigate.");
        }
    }
    
    @FXML
    private void handleBukaJurnal() {
        if (mainController != null) {
            mainController.showJurnal();
        } else {
            System.err.println("MainController is null, cannot navigate.");
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
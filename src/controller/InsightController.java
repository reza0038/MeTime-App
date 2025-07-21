package controller;
import model.MoodEntry;
import model.Jurnal;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.DatePicker;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class InsightController {

    @FXML private ComboBox<String> timeRangeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label insightMessageLabel;

    @FXML private BarChart<String, Number> moodFrequencyChart;
    @FXML private CategoryAxis moodXAxis;
    @FXML private NumberAxis moodYAxis;

    @FXML private BarChart<String, Number> jurnalEntryCountChart;
    @FXML private CategoryAxis jurnalXAxis;
    @FXML private NumberAxis jurnalYAxis;

    private static final String MOOD_HISTORY_FILE = "mood_history.xml";
    private static final String JURNAL_HISTORY_FILE = "jurnal_history.xml";

    private List<MoodEntry> allMoodEntries = new ArrayList<>();
    private List<Jurnal> allJurnalEntries = new ArrayList<>();

    @FXML
    public void initialize() {
        timeRangeComboBox.getItems().addAll("Minggu Terakhir", "Bulan Ini", "3 Bulan Terakhir", "Semua Waktu", "Rentang Kustom");
        timeRangeComboBox.setValue("Semua Waktu"); 
        timeRangeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handleTimeRangeChange(newVal));

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.disableProperty().bind(timeRangeComboBox.valueProperty().isNotEqualTo("Rentang Kustom"));
        endDatePicker.disableProperty().bind(timeRangeComboBox.valueProperty().isNotEqualTo("Rentang Kustom"));
        startDatePicker.setOnAction(event -> loadAndDisplayInsight());
        endDatePicker.setOnAction(event -> loadAndDisplayInsight());

        moodXAxis.setLabel("Suasana Hati");
        moodYAxis.setLabel("Frekuensi");
        moodYAxis.setTickUnit(1);
        moodYAxis.setForceZeroInRange(true);

        jurnalXAxis.setLabel("Periode");
        jurnalYAxis.setLabel("Jumlah Entri");
        jurnalYAxis.setTickUnit(1);
        jurnalYAxis.setForceZeroInRange(true);

        loadAllSharedData();

        loadAndDisplayInsight();
    }

    private XStream createMoodXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{MoodEntry.class, ArrayList.class, Date.class});
        xstream.alias("moodEntry", MoodEntry.class);
        xstream.alias("moodHistory", List.class);
        return xstream;
    }

    private XStream createJurnalXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{Jurnal.class, ArrayList.class, LocalDate.class, Boolean.class});
        xstream.alias("jurnalEntry", Jurnal.class);
        xstream.alias("jurnalHistory", List.class);
        return xstream;
    }

    private void loadAllSharedData() {
        XStream moodXstream = createMoodXStream();
        try (FileReader reader = new FileReader(MOOD_HISTORY_FILE)) {
            Object readObject = moodXstream.fromXML(reader);
            if (readObject instanceof List) {
                List<?> rawList = (List<?>) readObject;
                allMoodEntries = rawList.stream()
                                    .filter(item -> item instanceof MoodEntry && ((MoodEntry) item).isShared())
                                    .map(item -> (MoodEntry) item)
                                    .collect(Collectors.toList());
            } else if (readObject instanceof MoodEntry && ((MoodEntry)readObject).isShared()) {
                allMoodEntries.add((MoodEntry) readObject);
            }
        } catch (IOException e) {
            System.out.println("No shared mood history file found for insights: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error deserializing shared mood history for insights: " + e.getMessage());
        }

        // Muat Jurnal Entries (TIDAK ADA FILTER SHARED LAGI)
        XStream jurnalXstream = createJurnalXStream();
        try (FileReader reader = new FileReader(JURNAL_HISTORY_FILE)) {
            Object readObject = jurnalXstream.fromXML(reader);
            if (readObject instanceof List) {
                List<?> rawList = (List<?>) readObject;
                allJurnalEntries = rawList.stream()
                                    .filter(item -> item instanceof Jurnal) 
                                    .map(item -> (Jurnal) item)
                                    .collect(Collectors.toList());
            }
        } catch (IOException e) {
            System.out.println("No jurnal history file found for insights: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error deserializing jurnal history for insights: " + e.getMessage());
        }
    }

    private void handleTimeRangeChange(String selectedRange) {
        switch (selectedRange) {
            case "Minggu Terakhir":
                startDatePicker.setValue(LocalDate.now().minusWeeks(1));
                endDatePicker.setValue(LocalDate.now());
                break;
            case "Bulan Ini":
                startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
                endDatePicker.setValue(LocalDate.now());
                break;
            case "3 Bulan Terakhir":
                startDatePicker.setValue(LocalDate.now().minusMonths(3));
                endDatePicker.setValue(LocalDate.now());
                break;
            case "Semua Waktu":
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                break;
            case "Rentang Kustom":
                break;
        }
        loadAndDisplayInsight();
    }

    @FXML
    private void loadAndDisplayInsight() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        List<MoodEntry> filteredMoods = allMoodEntries.stream()
                .filter(entry -> {
                    LocalDate entryDate = entry.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    boolean afterStart = (startDate == null || !entryDate.isBefore(startDate));
                    boolean beforeEnd = (endDate == null || !entryDate.isAfter(endDate));
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());

        List<Jurnal> filteredJurnal = allJurnalEntries.stream()
                .filter(entry -> {
                    LocalDate entryDate = entry.getTanggal();
                    boolean afterStart = (startDate == null || !entryDate.isBefore(startDate));
                    boolean beforeEnd = (endDate == null || !entryDate.isAfter(endDate));
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());

        displayMoodFrequencyChart(filteredMoods);
        displayJurnalEntryCountChart(filteredJurnal);

        insightMessageLabel.setText(
            String.format("Menampilkan insight dari %d catatan mood (shared) dan %d entri jurnal (semua).",
            filteredMoods.size(), filteredJurnal.size())
        );
    }

    private void displayMoodFrequencyChart(List<MoodEntry> moods) {
        Map<String, Integer> moodCounts = new HashMap<>();
        for (MoodEntry entry : moods) {
            moodCounts.put(entry.getMood(), moodCounts.getOrDefault(entry.getMood(), 0) + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Frekuensi");

        List<String> sortedMoods = new ArrayList<>(moodCounts.keySet());
        sortedMoods.sort(String::compareTo);

        for (String mood : sortedMoods) {
            series.getData().add(new XYChart.Data<>(mood, moodCounts.get(mood)));
        }

        moodFrequencyChart.getData().clear();
        if (!series.getData().isEmpty()) {
            moodFrequencyChart.getData().add(series);
            moodFrequencyChart.setAnimated(true); 
            moodFrequencyChart.setTitle(""); 
        } else {
            moodFrequencyChart.setAnimated(false);
            moodFrequencyChart.setTitle("Tidak ada data mood (shared) dalam rentang ini");
        }
    }

    private void displayJurnalEntryCountChart(List<Jurnal> jurnalEntries) {
        Map<String, Integer> monthlyCounts = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("id", "ID"));

        for (Jurnal entry : jurnalEntries) {
            String monthYear = entry.getTanggal().format(formatter);
            monthlyCounts.put(monthYear, monthlyCounts.getOrDefault(monthYear, 0) + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Jumlah Entri");

        for (Map.Entry<String, Integer> entry : monthlyCounts.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        jurnalEntryCountChart.getData().clear();
        if (!series.getData().isEmpty()) {
            jurnalEntryCountChart.getData().add(series);
            jurnalEntryCountChart.setAnimated(true);
            jurnalEntryCountChart.setTitle("");
        } else {
            jurnalEntryCountChart.setAnimated(false);
            jurnalEntryCountChart.setTitle("Tidak ada data jurnal dalam rentang ini");
        }
    }
}

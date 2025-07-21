package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.AnyTypePermission;

import model.MoodEntry; 
import model.Jurnal; 
import model.User;  
import model.UserSession; 

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardChartsController implements Initializable {

    @FXML private Label sapaanLabel;
    @FXML private Label tanggalLabel;
    @FXML private StackedAreaChart<String, Number> moodDevelopmentAreaChart;
    @FXML private PieChart moodCompositionChart;
    @FXML private BarChart<String, Number> activityFrequencyChart;

    private User currentUser; 

    private List<MoodEntry> allMoodEntries = new ArrayList<>();
    private List<Jurnal> allJurnalEntries = new ArrayList<>(); 

    
    private static final String MOOD_HISTORY_FILE = "mood_history.xml";
    private static final String JURNAL_HISTORY_FILE = "jurnal_history.xml";
    private static final String TANTANGAN_HISTORY_FILE = "tantangan_progress.xml";


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.currentUser = UserSession.getCurrentUser(); 
        if (currentUser == null) {
            sapaanLabel.setText("Tidak ada pengguna yang login.");
            moodDevelopmentAreaChart.setVisible(false);
            moodCompositionChart.setVisible(false);
            activityFrequencyChart.setVisible(false);
            return;
        }

        setupHeader();

        loadAllDataForCharts(); 

        setupMoodDevelopmentAreaChart(allMoodEntries);
        setupMoodCompositionChart(allMoodEntries);
        setupActivityFrequencyChart(allJurnalEntries, allMoodEntries);
    }

    private void setupHeader() {
        sapaanLabel.setText("Dashboard Insight, " + currentUser.getUsername() + "!");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        tanggalLabel.setText(LocalDate.now().format(formatter));
    }


    private XStream createMoodXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{MoodEntry.class, ArrayList.class, Date.class, Boolean.class});
        xstream.alias("moodEntry", MoodEntry.class);
        xstream.alias("moodHistory", List.class);
        return xstream;
    }

    private XStream createJurnalXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{Jurnal.class, ArrayList.class, LocalDate.class, Boolean.class}); // Ganti jurnal jadi Jurnal
        xstream.alias("jurnalEntry", Jurnal.class); 
        xstream.alias("jurnalHistory", List.class);
        return xstream;
    }


    private void loadAllDataForCharts() {
        XStream moodXstream = createMoodXStream();
        allMoodEntries.clear(); 
        try (FileReader reader = new FileReader(MOOD_HISTORY_FILE)) {
            Object readObject = moodXstream.fromXML(reader);
            if (readObject instanceof List) {
                List<?> rawList = (List<?>) readObject;
                for (Object item : rawList) {
                    if (item instanceof MoodEntry) {
                        allMoodEntries.add((MoodEntry) item);
                    }
                }
            } else if (readObject instanceof MoodEntry) { 
                allMoodEntries.add((MoodEntry) readObject);
            }
        } catch (IOException e) {
            System.out.println("No existing mood history file found for charts: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error deserializing mood history for charts: " + e.getMessage());
        }

        XStream jurnalXstream = createJurnalXStream();
        allJurnalEntries.clear(); 
        try (FileReader reader = new FileReader(JURNAL_HISTORY_FILE)) {
            Object readObject = jurnalXstream.fromXML(reader);
            if (readObject instanceof List) {
                List<?> rawList = (List<?>) readObject;
                for (Object item : rawList) {
                    if (item instanceof Jurnal) { 
                        allJurnalEntries.add((Jurnal) item); 
                    }
                }
            } else if (readObject instanceof Jurnal) { 
                allJurnalEntries.add((Jurnal) readObject); 
            }
        } catch (IOException e) {
            System.out.println("No existing jurnal history file found for charts: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error deserializing jurnal history for charts: " + e.getMessage());
        }
    }


    
    private void setupMoodDevelopmentAreaChart(List<MoodEntry> entries) { 
        if (moodDevelopmentAreaChart == null || moodDevelopmentAreaChart.getXAxis() == null || moodDevelopmentAreaChart.getYAxis() == null) {
            System.out.println("Warning: moodDevelopmentAreaChart FXML elements not fully connected. Skipping chart setup.");
            return;
        }
        
        Set<String> positifMoods = Set.of("senang", "netral"); 

        LocalDate fourWeeksAgo = LocalDate.now().minusWeeks(4);
        Map<Integer, List<MoodEntry>> weeklyEntries = entries.stream()
                .filter(entry -> {
                    LocalDate entryDate = entry.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !entryDate.isBefore(fourWeeksAgo);
                })
                .collect(Collectors.groupingBy(
                        entry -> entry.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                ));
        
        List<Map.Entry<Integer, List<MoodEntry>>> sortedWeeks = new ArrayList<>(weeklyEntries.entrySet());
        sortedWeeks.sort(Map.Entry.comparingByKey());

        XYChart.Series<String, Number> positiveSeries = new XYChart.Series<>();
        positiveSeries.setName("Positif");
        XYChart.Series<String, Number> negativeSeries = new XYChart.Series<>();
        negativeSeries.setName("Negatif");

        int weekCounter = 1;
        for (Map.Entry<Integer, List<MoodEntry>> weekData : sortedWeeks) {
            long positiveCount = weekData.getValue().stream().filter(e -> positifMoods.contains(e.getMood())).count();
            long negativeCount = weekData.getValue().size() - positiveCount;

            String weekLabel = "Minggu " + weekCounter++;
            positiveSeries.getData().add(new XYChart.Data<>(weekLabel, positiveCount));
            negativeSeries.getData().add(new XYChart.Data<>(weekLabel, negativeCount));
        }

        moodDevelopmentAreaChart.getData().clear();
        if (!positiveSeries.getData().isEmpty() || !negativeSeries.getData().isEmpty()) {
            moodDevelopmentAreaChart.getData().addAll(negativeSeries, positiveSeries);
            moodDevelopmentAreaChart.setAnimated(true);
            moodDevelopmentAreaChart.setTitle("Perkembangan Mood (4 Minggu Terakhir)");
        } else {
            moodDevelopmentAreaChart.setAnimated(false);
            moodDevelopmentAreaChart.setTitle("Tidak ada data mood.");
        }
    }

    private void setupMoodCompositionChart(List<MoodEntry> entries) { 
        if (moodCompositionChart == null) {
            System.out.println("Warning: moodCompositionChart FXML element not fully connected. Skipping chart setup.");
            return;
        }

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        Map<String, Long> moodCounts = entries.stream()
                .filter(entry -> {
                    LocalDate entryDate = entry.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !entryDate.isBefore(thirtyDaysAgo);
                })
                .collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        moodCounts.forEach((mood, count) -> pieChartData.add(new PieChart.Data(mood + " (" + count + ")", count)));

        moodCompositionChart.setData(pieChartData);
        moodCompositionChart.setTitle("Komposisi Perasaanmu (30 Hari Terakhir)");
        moodCompositionChart.setLabelsVisible(true);
        moodCompositionChart.setLegendVisible(true);
    }

    private void setupActivityFrequencyChart(List<Jurnal> jurnalEntries, List<MoodEntry> moodEntries) { 
        if (activityFrequencyChart == null || activityFrequencyChart.getXAxis() == null || activityFrequencyChart.getYAxis() == null) {
            System.out.println("Warning: activityFrequencyChart FXML elements not fully connected. Skipping chart setup.");
            return;
        }

        Map<DayOfWeek, Integer> moodByDay = new EnumMap<>(DayOfWeek.class);
        Map<DayOfWeek, Integer> jurnalByDay = new EnumMap<>(DayOfWeek.class);

        for (DayOfWeek day : DayOfWeek.values()) {
            moodByDay.put(day, 0);
            jurnalByDay.put(day, 0);
        }

        for (MoodEntry entry : moodEntries) {
            DayOfWeek day = entry.getDate().toInstant().atZone(ZoneId.systemDefault()).getDayOfWeek();
            moodByDay.put(day, moodByDay.get(day) + 1);
        }
        for (Jurnal entry : jurnalEntries) { 
            DayOfWeek day = entry.getTanggal().getDayOfWeek();
            jurnalByDay.put(day, jurnalByDay.get(day) + 1);
        }

        XYChart.Series<String, Number> moodSeries = new XYChart.Series<>();
        moodSeries.setName("Catatan Mood");
        XYChart.Series<String, Number> jurnalSeries = new XYChart.Series<>();
        jurnalSeries.setName("Entri Jurnal");
        
        String[] namaHari = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        for (int i=0; i < days.length; i++) {
            moodSeries.getData().add(new XYChart.Data<>(namaHari[i], moodByDay.get(days[i])));
            jurnalSeries.getData().add(new XYChart.Data<>(namaHari[i], jurnalByDay.get(days[i])));
        }

        activityFrequencyChart.getData().clear();
        if (!moodSeries.getData().isEmpty() || !jurnalSeries.getData().isEmpty()) {
            activityFrequencyChart.getData().addAll(moodSeries, jurnalSeries);
            activityFrequencyChart.setAnimated(true);
            activityFrequencyChart.setTitle("Frekuensi Aktivitas per Hari");
        } else {
            activityFrequencyChart.setAnimated(false);
            activityFrequencyChart.setTitle("Tidak ada data aktivitas.");
        }
    }
}
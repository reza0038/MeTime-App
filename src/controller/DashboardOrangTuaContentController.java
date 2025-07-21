package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox; 
import model.*;
import model.JurnalManager;
import model.MoodManager;
import model.TantanganManager;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class DashboardOrangTuaContentController implements Initializable {

    
    @FXML private ComboBox<Profil> anakComboBox;
    @FXML private HBox contentHBox; 
    @FXML private Label pesanLabel;
    @FXML private Label sapaanOrangTuaLabel;
    @FXML private Label tanggalOrangTuaLabel;
    @FXML private Label totalSharedMoodsLabel;
    @FXML private Label lastSharedMoodLabel;
    @FXML private Label totalSharedJurnalsLabel;
    @FXML private Label lastSharedJurnalLabel;
    @FXML private Label totalSharedTantanganLabel;
    @FXML private Label lastSharedTantanganLabel;
    @FXML private PieChart moodCompositionChart; 

    private User orangTua;
    private List<Profil> daftarAnakProfil = new ArrayList<>();
    private DashboardOrangTuaController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.orangTua = UserSession.getCurrentUser();
        if (this.orangTua == null) {
            pesanLabel.setText("Sesi orang tua tidak ditemukan.");
            return;
        }

        setupHeader();
        cariAnakTerhubung();
        
        anakComboBox.setCellFactory(lv -> new ListCell<Profil>() {
            @Override
            protected void updateItem(Profil item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNamaLengkap());
            }
        });
        anakComboBox.setButtonCell(new ListCell<Profil>() {
             @Override
            protected void updateItem(Profil item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNamaLengkap());
            }
        });
        anakComboBox.getItems().setAll(daftarAnakProfil);

        anakComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tampilkanDataAnak(newVal);
            }
        });
    }
    
    public void setMainController(DashboardOrangTuaController mainController) {
        this.mainController = mainController;
    }
    
    private void setupHeader() {
        sapaanOrangTuaLabel.setText("Halo, " + orangTua.getUsername() + "!");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        tanggalOrangTuaLabel.setText(LocalDate.now().format(formatter));
    }

    private void cariAnakTerhubung() {
        File dataDir = new File("xml");
        File[] profilFiles = dataDir.listFiles((dir, name) -> name.startsWith("profil_") && name.endsWith(".xml"));
        if (profilFiles == null) return;
        
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{Profil.class, LocalDate.class});
        xstream.alias("profil", Profil.class);

        for (File file : profilFiles) {
            try (FileReader reader = new FileReader(file)) {
                Profil profil = (Profil) xstream.fromXML(reader);
                if (orangTua.getUsername().equals(profil.getUsernameWali())) {
                    daftarAnakProfil.add(profil);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void tampilkanDataAnak(Profil profilAnak) {
        contentHBox.setDisable(false);
        pesanLabel.setVisible(false);

        String usernameAnak = profilAnak.getUsername();
        
        
        List<MoodEntry> sharedMoods = MoodManager.getAllMoods().stream()
                .filter(m -> usernameAnak.equals(m.getUsername()) && m.isShared())
                .collect(Collectors.toList());
        totalSharedMoodsLabel.setText("Total Mood Dibagikan: " + sharedMoods.size());
        sharedMoods.stream()
                .max(Comparator.comparing(MoodEntry::getDate))
                .ifPresentOrElse(
                    lastMood -> lastSharedMoodLabel.setText("Mood Terakhir: " + lastMood.getMood()),
                    () -> lastSharedMoodLabel.setText("Mood Terakhir: N/A")
                );
        setupMoodCompositionChart(sharedMoods); 
        
        
        List<Jurnal> sharedJurnals = JurnalManager.getAllJurnals().stream()
                .filter(j -> usernameAnak.equals(j.getUsername()) && j.isShared())
                .collect(Collectors.toList());
        totalSharedJurnalsLabel.setText("Total Jurnal Dibagikan: " + sharedJurnals.size());
        sharedJurnals.stream()
                .max(Comparator.comparing(Jurnal::getTanggal))
                .ifPresentOrElse(
                    lastJurnal -> {
                        String preview = lastJurnal.getIsi().length() > 15 ? lastJurnal.getIsi().substring(0, 15) + "..." : lastJurnal.getIsi();
                        lastSharedJurnalLabel.setText("Jurnal Terakhir: \"" + preview + "\"");
                    },
                    () -> lastSharedJurnalLabel.setText("Jurnal Terakhir: N/A")
                );

        
        List<TantanganProgress> sharedTantangan = TantanganManager.getAllTantanganProgress().stream()
                .filter(p -> usernameAnak.equals(p.getNamaRemaja()) && p.isShared())
                .collect(Collectors.toList());
        totalSharedTantanganLabel.setText("Total Tantangan Dibagikan: " + sharedTantangan.size());
        sharedTantangan.stream()
                .max(Comparator.comparing(TantanganProgress::getWaktuSelesaiAktual, Comparator.nullsLast(Comparator.naturalOrder())))
                .ifPresentOrElse(
                    last -> lastSharedTantanganLabel.setText("Tantangan Terakhir: " + last.getTantangan().getJudul()),
                    () -> lastSharedTantanganLabel.setText("Tantangan Terakhir: N/A")
                );
    }
    
    
    private void setupMoodCompositionChart(List<MoodEntry> moods) {
        moodCompositionChart.getData().clear();
        if (moods == null || moods.isEmpty()) {
            moodCompositionChart.setData(FXCollections.observableArrayList(new PieChart.Data("Belum ada data", 1)));
            return;
        }
        
        Map<String, Long> moodCounts = moods.stream()
                .collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.counting()));
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        moodCounts.forEach((mood, count) -> pieChartData.add(new PieChart.Data(mood, count)));
        moodCompositionChart.setData(pieChartData);
    }
    
    @FXML
    private void handleLihatInsightDetail() {
        if (mainController != null) {
            mainController.showParentInsight();
        }
    }
}
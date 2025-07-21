package controller;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import model.Profil;
import model.MoodEntry;
import model.User;
import model.UserSession;
import model.UserManager;

public class DataAnakViewController {

    @FXML private ListView<Profil> anakListView;
    @FXML private Label detailAnakLabel;
    @FXML private TableView<MoodEntry> moodTableView;
    @FXML private TableColumn<MoodEntry, String> tanggalColumn;
    @FXML private TableColumn<MoodEntry, String> moodColumn;
    @FXML private TableColumn<MoodEntry, String> catatanColumn;

    private User orangTua;

    @FXML
    public void initialize() {
        this.orangTua = UserSession.getCurrentUser();
        if (this.orangTua == null) {
            detailAnakLabel.setText("Gagal memuat sesi orang tua.");
            return;
        }

   
        anakListView.setCellFactory(lv -> new ListCell<Profil>() {
            @Override
            protected void updateItem(Profil item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNamaLengkap());
            }
        });

        
        anakListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tampilkanDataAnak(newVal);
            }
        });
        
        muatProfilAnak();
    }

    private void muatProfilAnak() {
        File dataDir = new File("data");
        File[] profilFiles = dataDir.listFiles((dir, name) -> name.startsWith("profil_") && name.endsWith(".xml"));

        if (profilFiles == null) return;

        XStream xstream = createXStream();
        ObservableList<Profil> daftarAnak = FXCollections.observableArrayList();

        for (File file : profilFiles) {
            try (FileReader reader = new FileReader(file)) {
                Profil profil = (Profil) xstream.fromXML(reader);
             
                if (orangTua.getUsername().equals(profil.getUsernameWali())) {
                    daftarAnak.add(profil);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        anakListView.setItems(daftarAnak);
    }

    private void tampilkanDataAnak(Profil profilAnak) {
        User akunAnak = UserManager.getUserByUsername(profilAnak.getUsername());
        if (akunAnak == null) return;

        detailAnakLabel.setText("Data Mood " + profilAnak.getNamaLengkap() + " (yang dibagikan)");
        
        List<MoodEntry> sharedMoods = akunAnak.getMoodHistory().stream()
                .filter(MoodEntry::isShared)
                .collect(Collectors.toList());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
        tanggalColumn.setCellValueFactory(cellData -> new SimpleStringProperty(dateFormat.format(cellData.getValue().getDate())));
        moodColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMood()));
        catatanColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNote()));

        moodTableView.setItems(FXCollections.observableArrayList(sharedMoods));
    }

    private XStream createXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{Profil.class, LocalDate.class});
        xstream.alias("profil", Profil.class);
        return xstream;
    }
}
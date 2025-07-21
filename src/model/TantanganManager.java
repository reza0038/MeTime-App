package model;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TantanganManager {

    private static final String DEFINITIONS_FILE = "xml/tantangan_definitions.xml";
    private static final String PROGRESS_FILE = "xml/tantangan_progress.xml";

    private static List<Tantangan> customTantanganList = new ArrayList<>();
    private static List<TantanganProgress> allProgressList = new ArrayList<>();

    static {
        loadTantanganDefinitions();
        loadTantanganProgress();
    }

    private static XStream createXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{
            Tantangan.class, TantanganProgress.class, ArrayList.class,
            Duration.class, LocalDateTime.class, LocalDate.class
        });
        xstream.alias("tantangan", Tantangan.class);
        xstream.alias("daftarTantangan", List.class);
        xstream.alias("progress", TantanganProgress.class);
        xstream.alias("daftarProgress", List.class);
        return xstream;
    }


    public static void loadTantanganDefinitions() {
        File file = new File(DEFINITIONS_FILE);
        if (!file.exists() || file.length() == 0) return;
        try (FileReader reader = new FileReader(file)) {
            customTantanganList = (List<Tantangan>) createXStream().fromXML(reader);
        } catch (Exception e) { e.printStackTrace(); }
        if (customTantanganList == null) customTantanganList = new ArrayList<>();
    }

    public static void saveTantanganDefinitions() {
        try (FileWriter writer = new FileWriter(DEFINITIONS_FILE)) {
            createXStream().toXML(customTantanganList, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void addTantanganDefinition(Tantangan tantangan) {
        customTantanganList.add(tantangan);
        saveTantanganDefinitions();
    }

    public static List<Tantangan> getCustomTantangan() {
        return new ArrayList<>(customTantanganList);
    }

    
    public static void loadTantanganProgress() {
        File file = new File(PROGRESS_FILE);
        if (!file.exists() || file.length() == 0) return;
        try (FileReader reader = new FileReader(file)) {
            allProgressList = (List<TantanganProgress>) createXStream().fromXML(reader);
        } catch (Exception e) { e.printStackTrace(); }
        if (allProgressList == null) allProgressList = new ArrayList<>();
    }

    public static void saveTantanganProgress() {
        try (FileWriter writer = new FileWriter(PROGRESS_FILE)) {
            createXStream().toXML(allProgressList, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void addTantanganProgress(TantanganProgress progress) {
        allProgressList.add(progress);
        saveTantanganProgress();
    }
    
    public static List<TantanganProgress> getAllTantanganProgress() {
        return new ArrayList<>(allProgressList);
    }

    public static List<TantanganProgress> getTantanganProgressByUsername(String username) {
        return allProgressList.stream()
                .filter(p -> p.getNamaRemaja().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }
}
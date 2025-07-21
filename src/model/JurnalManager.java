package model;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JurnalManager {
    private static final String JURNAL_FILE = "xml/jurnal_history.xml";
    private static List<Jurnal> allJurnalsList = new ArrayList<>();

    static {
        loadJurnals();
    }
    
    private static XStream createXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(AnyTypePermission.ANY); // Izin dasar
        // Tipe data yang diizinkan untuk dibaca/ditulis
        xstream.allowTypes(new Class[]{ArrayList.class, Jurnal.class, LocalDate.class});
        xstream.alias("jurnals", List.class);
        xstream.alias("jurnalEntry", Jurnal.class);
        return xstream;
    }

    public static void loadJurnals() {
        File file = new File(JURNAL_FILE);
        if (!file.exists() || file.length() == 0) {
            allJurnalsList = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            allJurnalsList = (List<Jurnal>) createXStream().fromXML(reader);
        } catch (Exception e) {
            e.printStackTrace();
            allJurnalsList = new ArrayList<>();
        }
        
        if (allJurnalsList == null) {
            allJurnalsList = new ArrayList<>();
        }
    }

    public static void saveJurnals() {
        try {
            File file = new File(JURNAL_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            FileWriter writer = new FileWriter(file);
            createXStream().toXML(allJurnalsList, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addJurnal(Jurnal j) {
        allJurnalsList.add(j);
        saveJurnals();
    }
    
    public static void updateJurnal(Jurnal updatedJurnal) {
        // Logika ini mengasumsikan objek yang di-pass adalah referensi yang sama
        // atau Anda perlu logika pencarian yang lebih kompleks jika tidak.
        saveJurnals();
    }
    
    public static void deleteJurnal(Jurnal jurnalToDelete) {
        allJurnalsList.remove(jurnalToDelete);
        saveJurnals();
    }

    public static List<Jurnal> getJurnalsByUsername(String username) {
        if (username == null) return new ArrayList<>();
        return allJurnalsList.stream()
                .filter(j -> username.equalsIgnoreCase(j.getUsername()))
                .sorted(Comparator.comparing(Jurnal::getTanggal).reversed()) // Urutkan di sini
                .collect(Collectors.toList());
    }

    public static List<Jurnal> getAllJurnals() {
        return new ArrayList<>(allJurnalsList);
    }
}
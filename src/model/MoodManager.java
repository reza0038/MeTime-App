package model;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MoodManager {
    private static final String MOOD_FILE = "xml/mood_history.xml";
    private static List<MoodEntry> allMoodsList = new ArrayList<>();

    static {
        loadMoods();
    }

    private static XStream createXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.allowTypes(new Class[]{ArrayList.class, MoodEntry.class, Date.class, MoodEntry.TimeOfDay.class});
        xstream.alias("moods", List.class);
        xstream.alias("moodEntry", MoodEntry.class);
        return xstream;
    }

    public static void loadMoods() {
        File file = new File(MOOD_FILE);
        if (!file.exists() || file.length() == 0) {
            allMoodsList = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            allMoodsList = (List<MoodEntry>) createXStream().fromXML(reader);
        } catch (Exception e) {
            e.printStackTrace();
            allMoodsList = new ArrayList<>();
        }
        if (allMoodsList == null) {
            allMoodsList = new ArrayList<>();
        }
    }

    public static void saveMoods() {
        File file = new File(MOOD_FILE);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            createXStream().toXML(allMoodsList, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addMood(MoodEntry moodEntry) {
        allMoodsList.add(moodEntry);
        saveMoods();
    }

    public static void updateMood(MoodEntry updatedMood) {
        for (int i = 0; i < allMoodsList.size(); i++) {
            if (allMoodsList.get(i).getUsername().equals(updatedMood.getUsername()) &&
                allMoodsList.get(i).getDate().equals(updatedMood.getDate())) {
                allMoodsList.set(i, updatedMood);
                saveMoods();
                return;
            }
        }
    }

    // ### METHOD BARU UNTUK FITUR HAPUS ###
    public static void deleteMood(MoodEntry moodToDelete) {
        // Hapus entri dari list di memori
        allMoodsList.remove(moodToDelete);
        // Simpan list yang sudah diperbarui ke file XML
        saveMoods();
    }

    public static List<MoodEntry> getMoodsByUsername(String username) {
        if (username == null) return new ArrayList<>();
        return allMoodsList.stream()
                .filter(mood -> username.equalsIgnoreCase(mood.getUsername()))
                .collect(Collectors.toList());
    }

    public static List<MoodEntry> getAllMoods() {
        return new ArrayList<>(allMoodsList);
    }
}
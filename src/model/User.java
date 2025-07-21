package model;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String email;
    private String password;
    private String peran; 
    private List<MoodEntry> moodHistory;
    private List<Jurnal> jurnalHistory;

    public User() {
        moodHistory = new ArrayList<>();
        jurnalHistory = new ArrayList<>();
        this.peran = null; 
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.peran = null; 
        this.moodHistory = new ArrayList<>();
        this.jurnalHistory = new ArrayList<>();
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPeran() { return peran; } 
    public List<MoodEntry> getMoodHistory() { return moodHistory; }
    public List<Jurnal> getJurnalHistory() { return jurnalHistory; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setPeran(String peran) { this.peran = peran; } 
    public void setMoodHistory(List<MoodEntry> moodHistory) { this.moodHistory = moodHistory; }
    public void setJurnalHistory(List<Jurnal> jurnalHistory) { this.jurnalHistory = jurnalHistory; }
}
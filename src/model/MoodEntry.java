package model;

import java.io.Serializable;
import java.util.Date;
import java.util.Calendar;

public class MoodEntry implements Serializable {
    private String username; // <-- FIELD YANG DIBUTUHKAN
    private String mood;
    private String note;
    private boolean shared;
    private Date date;
    private TimeOfDay timeOfDay;

    public enum TimeOfDay {
        MORNING, AFTERNOON, EVENING, NIGHT
    }

    // Konstruktor yang otomatis menentukan TimeOfDay
    public MoodEntry(String username, String mood, String note, boolean shared, Date date) {
        this.username = username;
        this.mood = mood;
        this.note = note;
        this.shared = shared;
        this.date = date;
        this.timeOfDay = determineTimeOfDay(date);
    }
    
    // Getters
    public String getUsername() { return username; } // <-- METHOD YANG DIBUTUHKAN
    public String getMood() { return mood; }
    public String getNote() { return note; }
    public boolean isShared() { return shared; }
    public Date getDate() { return date; }
    public TimeOfDay getTimeOfDay() { return timeOfDay; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setMood(String mood) { this.mood = mood; }
    public void setNote(String note) { this.note = note; }
    public void setShared(boolean shared) { this.shared = shared; }
    public void setDate(Date date) { this.date = date; }
    public void setTimeOfDay(TimeOfDay timeOfDay) { this.timeOfDay = timeOfDay; }

    // Helper method untuk menentukan TimeOfDay dari sebuah Date
    private static TimeOfDay determineTimeOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return TimeOfDay.MORNING;
        } else if (hour >= 12 && hour < 17) {
            return TimeOfDay.AFTERNOON;
        } else if (hour >= 17 && hour < 21) {
            return TimeOfDay.EVENING;
        } else {
            return TimeOfDay.NIGHT;
        }
    }
}
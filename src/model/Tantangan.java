package model;
import java.time.Duration;

public class Tantangan {
    private String judul;
    private String deskripsi;
    private Duration durasiTarget;
    private String targetRole; 

    
    public Tantangan(String judul, String deskripsi, Duration durasiTarget, String targetRole) {
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.durasiTarget = durasiTarget;
        this.targetRole = targetRole;
    }

    public Tantangan(String judul, String deskripsi, Duration durasiTarget) {
        this(judul, deskripsi, durasiTarget, "Semua"); 
    }

    public String getJudul() { return judul; }
    public String getDeskripsi() { return deskripsi; }
    public Duration getDurasiTarget() { return durasiTarget; }
    public String getTargetRole() { return targetRole; } 

    public void setJudul(String judul) { this.judul = judul; } 
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; } 
    public void setDurasiTarget(Duration durasiTarget) { this.durasiTarget = durasiTarget; } 
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; } 

    public String getDurasiTargetFormatted() {
        if (durasiTarget == null) return "Tidak Ditentukan";
        long seconds = durasiTarget.getSeconds();
        if (seconds == 0) return "Tidak Ditentukan";
        if (seconds < 60) return seconds + " detik";
        if (seconds < 3600) return (seconds / 60) + " menit";
        return String.format("%d jam %d menit", seconds / 3600, (seconds % 3600) / 60);
    }
}
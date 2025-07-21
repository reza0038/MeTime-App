package model;
import java.time.LocalDateTime; 
import java.time.Duration; 

public class TantanganProgress {
    private Tantangan tantangan; 
    private String namaRemaja;   
    private LocalDateTime waktuMulaiAktual; 
    private LocalDateTime waktuSelesaiAktual; 
    private Duration durasiPengerjaan; 
    private boolean isSelesai;      
    private String feedbackPengguna; 
    private boolean shared;         

    public TantanganProgress(Tantangan tantangan, String namaRemaja, LocalDateTime waktuMulaiAktual, boolean isSelesai, String feedbackPengguna, boolean shared) {
        this.tantangan = tantangan;
        this.namaRemaja = namaRemaja;
        this.waktuMulaiAktual = waktuMulaiAktual;
        this.isSelesai = isSelesai;
        this.feedbackPengguna = feedbackPengguna;
        this.shared = shared;
        
        this.waktuSelesaiAktual = null;
        this.durasiPengerjaan = null;
    }

    
    public Tantangan getTantangan() { return tantangan; }
    public String getNamaRemaja() { return namaRemaja; }
    public LocalDateTime getWaktuMulaiAktual() { return waktuMulaiAktual; }
    public LocalDateTime getWaktuSelesaiAktual() { return waktuSelesaiAktual; }
    public Duration getDurasiPengerjaan() { return durasiPengerjaan; }
    public boolean isSelesai() { return isSelesai; }
    public String getFeedbackPengguna() { return feedbackPengguna; }
    public boolean isShared() { return shared; }

    
    public void setTantangan(Tantangan tantangan) { this.tantangan = tantangan; }
    public void setNamaRemaja(String namaRemaja) { this.namaRemaja = namaRemaja; }
    public void setWaktuMulaiAktual(LocalDateTime waktuMulaiAktual) { this.waktuMulaiAktual = waktuMulaiAktual; }
    public void setWaktuSelesaiAktual(LocalDateTime waktuSelesaiAktual) {
        this.waktuSelesaiAktual = waktuSelesaiAktual;
        if (waktuMulaiAktual != null && waktuSelesaiAktual != null) {
            this.durasiPengerjaan = Duration.between(waktuMulaiAktual, waktuSelesaiAktual);
        }
    }
    public void setDurasiPengerjaan(Duration durasiPengerjaan) { this.durasiPengerjaan = durasiPengerjaan; }
    public void setSelesai(boolean selesai) { isSelesai = selesai; }
    public void setFeedbackPengguna(String feedbackPengguna) { this.feedbackPengguna = feedbackPengguna; }
    public void setShared(boolean shared) { this.shared = shared; }

    
    public String getDurasiPengerjaanFormatted() {
        if (durasiPengerjaan == null) return "N/A";
        long seconds = durasiPengerjaan.getSeconds();
        return String.format("%d jam %d menit %d detik",
                             seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }
}

package model;

import java.time.LocalDate;

public class Profil {
    private String username; // Username pemilik profil (remaja)
    private String namaLengkap;
    private LocalDate tanggalLahir;
    private String jenisKelamin;
    private String alamatEmail;
    private String nomorTelepon;
    private String usernameWali; // Username akun orang tua/wali

    public Profil(String username, String namaLengkap, LocalDate tanggalLahir, String jenisKelamin, String alamatEmail, String nomorTelepon, String usernameWali) {
        this.username = username;
        this.namaLengkap = namaLengkap;
        this.tanggalLahir = tanggalLahir;
        this.jenisKelamin = jenisKelamin;
        this.alamatEmail = alamatEmail;
        this.nomorTelepon = nomorTelepon;
        this.usernameWali = usernameWali;
    }

    // Getters
    public String getUsername() { return username; }
    public String getNamaLengkap() { return namaLengkap; }
    public LocalDate getTanggalLahir() { return tanggalLahir; }
    public String getJenisKelamin() { return jenisKelamin; }
    public String getAlamatEmail() { return alamatEmail; }
    public String getNomorTelepon() { return nomorTelepon; }
    public String getUsernameWali() { return usernameWali; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }
    public void setTanggalLahir(LocalDate tanggalLahir) { this.tanggalLahir = tanggalLahir; }
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }
    public void setAlamatEmail(String alamatEmail) { this.alamatEmail = alamatEmail; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }
    public void setUsernameWali(String usernameWali) { this.usernameWali = usernameWali; }
}
package model;

import java.time.LocalDate;

public class Jurnal {
    private String username;
    private LocalDate tanggal;
    private String isi;
    private boolean shared; // <<< PERBAIKAN: Properti 'shared' ditambahkan kembali

    // <<< PERBAIKAN: Konstruktor kini menerima 'shared' >>>
    public Jurnal(String username, LocalDate tanggal, String isi, boolean shared) {
        this.username = username;
        this.tanggal = tanggal;
        this.isi = isi;
        this.shared = shared;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public String getIsi() {
        return isi;
    }

    public void setIsi(String isi) {
        this.isi = isi;
    }

    // <<< PERBAIKAN: Metode getter untuk 'shared' (isShared) >>>
    public boolean isShared() {
        return shared;
    }

    // <<< PERBAIKAN: Metode setter untuk 'shared' >>>
    public void setShared(boolean shared) {
        this.shared = shared;
    }
}
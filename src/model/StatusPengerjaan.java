package model;
public class StatusPengerjaan {
    private String namaUser;
    private String peran;
    private String judulTantangan;
    private String status;

    public StatusPengerjaan(String namaUser, String peran, String judulTantangan, String status) {
        this.namaUser = namaUser;
        this.peran = peran;
        this.judulTantangan = judulTantangan;
        this.status = status;
    }

    public String getNamaUser() { return namaUser; }
    public void setNamaUser(String namaUser) { this.namaUser = namaUser; }

    public String getPeran() { return peran; }
    public void setPeran(String peran) { this.peran = peran; }

    public String getJudulTantangan() { return judulTantangan; }
    public void setJudulTantangan(String judulTantangan) { this.judulTantangan = judulTantangan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

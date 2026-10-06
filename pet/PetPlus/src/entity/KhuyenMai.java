package entity;

import java.time.LocalDate;

public class KhuyenMai {
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private float phanTramGiam;
    private String dieuKienApDung;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;

    public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, float phanTramGiam, String dieuKienApDung,
            LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.phanTramGiam = phanTramGiam;
        this.dieuKienApDung = dieuKienApDung;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public KhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public KhuyenMai() {
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getTenKhuyenMai() {
        return tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }

    public float getPhanTramGiam() {
        return phanTramGiam;
    }

    public void setPhanTramGiam(float phanTramGiam) {
        this.phanTramGiam = phanTramGiam;
    }

    public String getDieuKienApDung() {
        return dieuKienApDung;
    }

    public void setDieuKienApDung(String dieuKienApDung) {
        this.dieuKienApDung = dieuKienApDung;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maKhuyenMai == null) ? 0 : maKhuyenMai.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KhuyenMai other = (KhuyenMai) obj;
        if (maKhuyenMai == null) {
            if (other.maKhuyenMai != null)
                return false;
        } else if (!maKhuyenMai.equals(other.maKhuyenMai))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KhuyenMai [maKhuyenMai=" + maKhuyenMai + ", tenKhuyenMai=" + tenKhuyenMai + ", phanTramGiam="
                + phanTramGiam + ", dieuKienApDung=" + dieuKienApDung + ", ngayBatDau=" + ngayBatDau + ", ngayKetThuc="
                + ngayKetThuc + "]";
    }

}

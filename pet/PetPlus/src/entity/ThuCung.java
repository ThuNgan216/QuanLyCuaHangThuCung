package entity;

import java.time.LocalDate;

public class ThuCung {
    private String maThuCung;
    private String tenThuCung;
    private String loai;
    private String giong;
    private String gioiTinh;
    private float canNang;
    private LocalDate ngaySinh;
    private String tinhTrangSK;
    private String tiemChung;
    private KhachHang maKH;

    public ThuCung() {
    }

    public ThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    public ThuCung(String maThuCung, String tenThuCung, String loai, String giong, String gioiTinh, float canNang,
            LocalDate ngaySinh, String tinhTrangSK, String tiemChung, KhachHang maKH) {
        this.maThuCung = maThuCung;
        this.tenThuCung = tenThuCung;
        this.loai = loai;
        this.giong = giong;
        this.gioiTinh = gioiTinh;
        this.canNang = canNang;
        this.ngaySinh = ngaySinh;
        this.tinhTrangSK = tinhTrangSK;
        this.tiemChung = tiemChung;
        this.maKH = maKH;
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public String getGiong() {
        return giong;
    }

    public void setGiong(String giong) {
        this.giong = giong;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public float getCanNang() {
        return canNang;
    }

    public void setCanNang(float canNang) {
        this.canNang = canNang;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getTinhTrangSK() {
        return tinhTrangSK;
    }

    public void setTinhTrangSK(String tinhTrangSK) {
        this.tinhTrangSK = tinhTrangSK;
    }

    public String getTiemChung() {
        return tiemChung;
    }

    public void setTiemChung(String tiemChung) {
        this.tiemChung = tiemChung;
    }

    public KhachHang getMaKH() {
        return maKH;
    }

    public void setMaKH(KhachHang maKH) {
        this.maKH = maKH;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maThuCung == null) ? 0 : maThuCung.hashCode());
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
        ThuCung other = (ThuCung) obj;
        if (maThuCung == null) {
            if (other.maThuCung != null)
                return false;
        } else if (!maThuCung.equals(other.maThuCung))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ThuCung [maThuCung=" + maThuCung + ", tenThuCung=" + tenThuCung + ", loai=" + loai + ", giong=" + giong
                + ", gioiTinh=" + gioiTinh + ", canNang=" + canNang + ", ngaySinh=" + ngaySinh + ", tinhTrangSK="
                + tinhTrangSK + ", tiemChung=" + tiemChung + ", maKH=" + maKH + "]";
    }

}

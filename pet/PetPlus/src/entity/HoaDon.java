package entity;

import java.time.LocalDate;

public class HoaDon {
    private String maHoaDon;
    private LocalDate ngayLapHD;
    private NhanVien maNV;
    private KhachHang maKH;
    private KhuyenMai maKhuyenMai;
    private PhuongThucThanhToan maPTTT;

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, LocalDate ngayLapHD, NhanVien maNV, KhachHang maKH, KhuyenMai maKhuyenMai,
            PhuongThucThanhToan maPTTT) {
        this.maHoaDon = maHoaDon;
        this.ngayLapHD = ngayLapHD;
        this.maNV = maNV;
        this.maKH = maKH;
        this.maKhuyenMai = maKhuyenMai;
        this.maPTTT = maPTTT;
    }

    public HoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public LocalDate getNgayLapHD() {
        return ngayLapHD;
    }

    public void setNgayLapHD(LocalDate ngayLapHD) {
        this.ngayLapHD = ngayLapHD;
    }

    public NhanVien getMaNV() {
        return maNV;
    }

    public void setMaNV(NhanVien maNV) {
        this.maNV = maNV;
    }

    public KhachHang getMaKH() {
        return maKH;
    }

    public void setMaKH(KhachHang maKH) {
        this.maKH = maKH;
    }

    public KhuyenMai getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(KhuyenMai maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public PhuongThucThanhToan getMaPTTT() {
        return maPTTT;
    }

    public void setMaPTTT(PhuongThucThanhToan maPTTT) {
        this.maPTTT = maPTTT;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maHoaDon == null) ? 0 : maHoaDon.hashCode());
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
        HoaDon other = (HoaDon) obj;
        if (maHoaDon == null) {
            if (other.maHoaDon != null)
                return false;
        } else if (!maHoaDon.equals(other.maHoaDon))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "HoaDon [maHoaDon=" + maHoaDon + ", ngayLapHD=" + ngayLapHD + ", maNV=" + maNV + ", maKH=" + maKH
                + ", maKhuyenMai=" + maKhuyenMai + ", maPTTT=" + maPTTT + "]";
    }

}

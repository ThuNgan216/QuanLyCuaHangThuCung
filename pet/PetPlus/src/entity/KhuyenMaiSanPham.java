package entity;

import java.math.BigDecimal;

public class KhuyenMaiSanPham {
    private String maKhuyenMaiMaSanPham;
    private BigDecimal mucGiam;
    private SanPham maSP;
    private KhuyenMai maKhuyenMai;
    private HoaDon maHoaDon;

    public KhuyenMaiSanPham(String maKhuyenMaiMaSanPham, BigDecimal mucGiam, SanPham maSP, KhuyenMai maKhuyenMai,
            HoaDon maHoaDon) {
        this.maKhuyenMaiMaSanPham = maKhuyenMaiMaSanPham;
        this.mucGiam = mucGiam;
        this.maSP = maSP;
        this.maKhuyenMai = maKhuyenMai;
        this.maHoaDon = maHoaDon;
    }

    public KhuyenMaiSanPham(String maKhuyenMaiMaSanPham) {
        this.maKhuyenMaiMaSanPham = maKhuyenMaiMaSanPham;
    }

    public KhuyenMaiSanPham() {
    }

    public String getMaKhuyenMaiMaSanPham() {
        return maKhuyenMaiMaSanPham;
    }

    public void setMaKhuyenMaiMaSanPham(String maKhuyenMaiMaSanPham) {
        this.maKhuyenMaiMaSanPham = maKhuyenMaiMaSanPham;
    }

    public BigDecimal getMucGiam() {
        return mucGiam;
    }

    public void setMucGiam(BigDecimal mucGiam) {
        this.mucGiam = mucGiam;
    }

    public SanPham getMaSP() {
        return maSP;
    }

    public void setMaSP(SanPham maSP) {
        this.maSP = maSP;
    }

    public KhuyenMai getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(KhuyenMai maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public HoaDon getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(HoaDon maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maKhuyenMaiMaSanPham == null) ? 0 : maKhuyenMaiMaSanPham.hashCode());
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
        KhuyenMaiSanPham other = (KhuyenMaiSanPham) obj;
        if (maKhuyenMaiMaSanPham == null) {
            if (other.maKhuyenMaiMaSanPham != null)
                return false;
        } else if (!maKhuyenMaiMaSanPham.equals(other.maKhuyenMaiMaSanPham))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KhuyenMaiSanPham [maKhuyenMaiMaSanPham=" + maKhuyenMaiMaSanPham + ", mucGiam=" + mucGiam + ", maSP="
                + maSP + ", maKhuyenMai=" + maKhuyenMai + ", maHoaDon=" + maHoaDon + "]";
    }

}

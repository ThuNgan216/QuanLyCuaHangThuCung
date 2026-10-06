package entity;

import java.math.BigDecimal;

public class ChiTietHoaDon {
    private HoaDon maHoaDon;
    private DichVu maDichVu;
    private int soLuong;
    private BigDecimal donGia;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(HoaDon maHoaDon, DichVu maDichVu) {
        this.maHoaDon = maHoaDon;
        this.maDichVu = maDichVu;
    }

    public ChiTietHoaDon(HoaDon maHoaDon, DichVu maDichVu, int soLuong, BigDecimal donGia) {
        this.maHoaDon = maHoaDon;
        this.maDichVu = maDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public HoaDon getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(HoaDon maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu maDichVu) {
        this.maDichVu = maDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maHoaDon == null) ? 0 : maHoaDon.hashCode());
        result = prime * result + ((maDichVu == null) ? 0 : maDichVu.hashCode());
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
        ChiTietHoaDon other = (ChiTietHoaDon) obj;
        if (maHoaDon == null) {
            if (other.maHoaDon != null)
                return false;
        } else if (!maHoaDon.equals(other.maHoaDon))
            return false;
        if (maDichVu == null) {
            if (other.maDichVu != null)
                return false;
        } else if (!maDichVu.equals(other.maDichVu))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon [maHoaDon=" + maHoaDon + ", maDichVu=" + maDichVu + ", soLuong=" + soLuong + ", donGia="
                + donGia + "]";
    }

}

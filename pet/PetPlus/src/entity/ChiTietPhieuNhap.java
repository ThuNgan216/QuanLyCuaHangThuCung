package entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ChiTietPhieuNhap {
    private int soLuongNhap;
    private BigDecimal donGiaNhap;
    private LocalDate hanSuDung;
    private PhieuNhap maPN;
    private SanPham maSP;

    public ChiTietPhieuNhap() {
    }

    public ChiTietPhieuNhap(PhieuNhap maPN, SanPham maSP) {
        this.maPN = maPN;
        this.maSP = maSP;
    }

    public ChiTietPhieuNhap(int soLuongNhap, BigDecimal donGiaNhap, LocalDate hanSuDung, PhieuNhap maPN, SanPham maSP) {
        this.soLuongNhap = soLuongNhap;
        this.donGiaNhap = donGiaNhap;
        this.hanSuDung = hanSuDung;
        this.maPN = maPN;
        this.maSP = maSP;
    }

    public int getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(int soLuongNhap) {
        this.soLuongNhap = soLuongNhap;
    }

    public BigDecimal getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(BigDecimal donGiaNhap) {
        this.donGiaNhap = donGiaNhap;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public PhieuNhap getMaPN() {
        return maPN;
    }

    public void setMaPN(PhieuNhap maPN) {
        this.maPN = maPN;
    }

    public SanPham getMaSP() {
        return maSP;
    }

    public void setMaSP(SanPham maSP) {
        this.maSP = maSP;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maPN == null) ? 0 : maPN.hashCode());
        result = prime * result + ((maSP == null) ? 0 : maSP.hashCode());
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
        ChiTietPhieuNhap other = (ChiTietPhieuNhap) obj;
        if (maPN == null) {
            if (other.maPN != null)
                return false;
        } else if (!maPN.equals(other.maPN))
            return false;
        if (maSP == null) {
            if (other.maSP != null)
                return false;
        } else if (!maSP.equals(other.maSP))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChiTietPhieuNhap [soLuongNhap=" + soLuongNhap + ", donGiaNhap=" + donGiaNhap + ", hanSuDung="
                + hanSuDung + ", maPN=" + maPN + ", maSP=" + maSP + "]";
    }

}

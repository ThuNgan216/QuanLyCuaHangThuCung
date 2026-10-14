package entity;

import java.math.BigDecimal;

public class ChiTietHoaDon {
    private HoaDon maHoaDon;
    private DichVu maDichVu;
    private SanPham maSP;
    private int soLuong;
    private BigDecimal donGia;
    private BigDecimal tienKhachDua;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(HoaDon maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public ChiTietHoaDon(HoaDon maHoaDon, DichVu maDichVu, SanPham maSP, int soLuong, BigDecimal donGia,
            BigDecimal tienKhachDua) {
        this.maHoaDon = maHoaDon;
        this.maDichVu = maDichVu;
        this.maSP = maSP;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.tienKhachDua = tienKhachDua;
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

    public SanPham getMaSP() {
        return maSP;
    }

    public void setMaSP(SanPham maSP) {
        this.maSP = maSP;
    }

    public BigDecimal getTienKhachDua() {
        return tienKhachDua;
    }

    public void setTienKhachDua(BigDecimal tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

}

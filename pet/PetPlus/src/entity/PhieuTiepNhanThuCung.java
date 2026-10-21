package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Thông tin phiếu tiếp nhận thực tế khi thú cưng được bàn giao tại cửa hàng. */
public class PhieuTiepNhanThuCung {
    private String maTiepNhan;
    private String maLichHen;
    private LocalDateTime thoiGianTiepNhan;
    private LocalDateTime gioHenTra;
    private String maNV;
    private String tenNV;
    private String maKH;
    private String tenKH;
    private String sdtKH;
    private String maThuCung;
    private String tenThuCung;
    private String loaiThuCung;
    private String dichVuYeuCau;
    private String tinhTrangBanGiao;
    private String ghiChu;
    private BigDecimal phiDuKien;
    private List<DichVu> danhSachDichVu = new ArrayList<>();
    private String trangThai;

    public String getMaTiepNhan() { return maTiepNhan; }
    public void setMaTiepNhan(String maTiepNhan) { this.maTiepNhan = maTiepNhan; }
    public String getMaLichHen() { return maLichHen; }
    public void setMaLichHen(String maLichHen) { this.maLichHen = maLichHen; }
    public LocalDateTime getThoiGianTiepNhan() { return thoiGianTiepNhan; }
    public void setThoiGianTiepNhan(LocalDateTime thoiGianTiepNhan) { this.thoiGianTiepNhan = thoiGianTiepNhan; }
    public LocalDateTime getGioHenTra() { return gioHenTra; }
    public void setGioHenTra(LocalDateTime gioHenTra) { this.gioHenTra = gioHenTra; }
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }
    public String getSdtKH() { return sdtKH; }
    public void setSdtKH(String sdtKH) { this.sdtKH = sdtKH; }
    public String getMaThuCung() { return maThuCung; }
    public void setMaThuCung(String maThuCung) { this.maThuCung = maThuCung; }
    public String getTenThuCung() { return tenThuCung; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }
    public String getLoaiThuCung() { return loaiThuCung; }
    public void setLoaiThuCung(String loaiThuCung) { this.loaiThuCung = loaiThuCung; }
    public String getDichVuYeuCau() { return dichVuYeuCau; }
    public void setDichVuYeuCau(String dichVuYeuCau) { this.dichVuYeuCau = dichVuYeuCau; }
    public String getTinhTrangBanGiao() { return tinhTrangBanGiao; }
    public void setTinhTrangBanGiao(String tinhTrangBanGiao) { this.tinhTrangBanGiao = tinhTrangBanGiao; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public BigDecimal getPhiDuKien() { return phiDuKien; }
    public void setPhiDuKien(BigDecimal phiDuKien) { this.phiDuKien = phiDuKien; }
    public List<DichVu> getDanhSachDichVu() { return danhSachDichVu; }
    public void setDanhSachDichVu(List<DichVu> danhSachDichVu) {
        this.danhSachDichVu = danhSachDichVu == null ? new ArrayList<>() : new ArrayList<>(danhSachDichVu);
    }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

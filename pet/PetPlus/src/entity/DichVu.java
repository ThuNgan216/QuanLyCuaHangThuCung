package entity;

import java.math.BigDecimal;

/** Danh mục dịch vụ của PetPlus. */
public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private BigDecimal donGia;
    private String moTa;
    private String trangThai;

    public DichVu() {}
    public DichVu(String maDichVu) { this.maDichVu = maDichVu; }
    public DichVu(String maDichVu, String tenDichVu, BigDecimal donGia, String moTa, String trangThai) {
        this.maDichVu = maDichVu; this.tenDichVu = tenDichVu; this.donGia = donGia;
        this.moTa = moTa; this.trangThai = trangThai;
    }
    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }
    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    @Override public int hashCode() { return maDichVu == null ? 0 : maDichVu.hashCode(); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DichVu)) return false;
        DichVu other = (DichVu) obj;
        return maDichVu == null ? other.maDichVu == null : maDichVu.equals(other.maDichVu);
    }
    @Override public String toString() { return tenDichVu == null ? "" : tenDichVu; }
}

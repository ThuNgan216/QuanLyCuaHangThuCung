package entity;

import java.math.BigDecimal;

public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private BigDecimal donGia;
    private String moTa;
    private String trangThai;
    private LichHen maLichHen;

    public DichVu() {
    }

    public DichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public DichVu(String maDichVu, String tenDichVu, BigDecimal donGia, String moTa, String trangThai,
            LichHen maLichHen) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.donGia = donGia;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.maLichHen = maLichHen;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LichHen getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(LichHen maLichHen) {
        this.maLichHen = maLichHen;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        DichVu other = (DichVu) obj;
        if (maDichVu == null) {
            if (other.maDichVu != null)
                return false;
        } else if (!maDichVu.equals(other.maDichVu))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DichVu [maDichVu=" + maDichVu + ", tenDichVu=" + tenDichVu + ", donGia=" + donGia + ", moTa=" + moTa
                + ", trangThai=" + trangThai + ", maLichHen=" + maLichHen + "]";
    }

}

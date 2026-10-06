package entity;

import java.time.LocalDateTime;

public class LichHen {
    private String maLichHen;
    private LocalDateTime thoiGianHen;
    private String trangThai;
    private KhachHang maKH;
    private NhanVien maNV;
    private ThuCung maThuCung;

    public LichHen(String maLichHen, LocalDateTime thoiGianHen, String trangThai, KhachHang maKH, NhanVien maNV,
            ThuCung maThuCung) {
        this.maLichHen = maLichHen;
        this.thoiGianHen = thoiGianHen;
        this.trangThai = trangThai;
        this.maKH = maKH;
        this.maNV = maNV;
        this.maThuCung = maThuCung;
    }

    public LichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public LichHen() {
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(LocalDateTime thoiGianHen) {
        this.thoiGianHen = thoiGianHen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public KhachHang getMaKH() {
        return maKH;
    }

    public void setMaKH(KhachHang maKH) {
        this.maKH = maKH;
    }

    public NhanVien getMaNV() {
        return maNV;
    }

    public void setMaNV(NhanVien maNV) {
        this.maNV = maNV;
    }

    public ThuCung getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(ThuCung maThuCung) {
        this.maThuCung = maThuCung;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maLichHen == null) ? 0 : maLichHen.hashCode());
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
        LichHen other = (LichHen) obj;
        if (maLichHen == null) {
            if (other.maLichHen != null)
                return false;
        } else if (!maLichHen.equals(other.maLichHen))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LichHen [maLichHen=" + maLichHen + ", thoiGianHen=" + thoiGianHen + ", trangThai=" + trangThai
                + ", maKH=" + maKH + ", maNV=" + maNV + ", maThuCung=" + maThuCung + "]";
    }

}
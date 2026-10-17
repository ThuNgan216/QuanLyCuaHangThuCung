package entity;

import java.time.LocalDate;

public class TichDiem {
    private String maTichDiem;
    private int diemTichLuy;
    private LocalDate ngayCapNhat;
    private KhachHang maKH;
    private HoaDon maHoaDon;

    public TichDiem() {
    }

    public TichDiem(String maTichDiem) {
        this.maTichDiem = maTichDiem;
    }

    public TichDiem(String maTichDiem, int diemTichLuy, LocalDate ngayCapNhat, KhachHang maKH, HoaDon maHoaDon) {
        this.maTichDiem = maTichDiem;
        this.diemTichLuy = diemTichLuy;
        this.ngayCapNhat = ngayCapNhat;
        this.maKH = maKH;
        this.maHoaDon = maHoaDon;
    }

    public String getMaTichDiem() {
        return maTichDiem;
    }

    public void setMaTichDiem(String maTichDiem) {
        this.maTichDiem = maTichDiem;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public LocalDate getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDate ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public KhachHang getMaKH() {
        return maKH;
    }

    public void setMaKH(KhachHang maKH) {
        this.maKH = maKH;
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
        result = prime * result + ((maTichDiem == null) ? 0 : maTichDiem.hashCode());
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
        TichDiem other = (TichDiem) obj;
        if (maTichDiem == null) {
            if (other.maTichDiem != null)
                return false;
        } else if (!maTichDiem.equals(other.maTichDiem))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TichDiem [maTichDiem=" + maTichDiem + ", diemTichLuy=" + diemTichLuy + ", ngayCapNhat=" + ngayCapNhat
                + ", maKH=" + maKH + ", maHoaDon=" + maHoaDon + "]";
    }

}

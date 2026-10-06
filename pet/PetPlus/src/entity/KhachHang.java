package entity;

public class KhachHang {
    private String maKH;
    private String sdt;
    private String diaChi;
    private int hangThanhVien;
    private int diemTichLuy;

    public KhachHang() {
    }

    public KhachHang(String maKH) {
        this.maKH = maKH;
    }

    public KhachHang(String maKH, String sdt, String diaChi, int hangThanhVien, int diemTichLuy) {
        this.maKH = maKH;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.hangThanhVien = hangThanhVien;
        this.diemTichLuy = diemTichLuy;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public int getHangThanhVien() {
        return hangThanhVien;
    }

    public void setHangThanhVien(int hangThanhVien) {
        this.hangThanhVien = hangThanhVien;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maKH == null) ? 0 : maKH.hashCode());
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
        KhachHang other = (KhachHang) obj;
        if (maKH == null) {
            if (other.maKH != null)
                return false;
        } else if (!maKH.equals(other.maKH))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KhachHang [maKH=" + maKH + ", sdt=" + sdt + ", diaChi=" + diaChi + ", hangThanhVien=" + hangThanhVien
                + ", diemTichLuy=" + diemTichLuy + "]";
    }

}

package entity;

import java.time.LocalDate;

public class PhieuNhap {
    private String maPN;
    private LocalDate ngayNhap;
    private NhanVien maNV;
    private NhaCungCap maNCC;

    public PhieuNhap() {
    }

    public PhieuNhap(String maPN) {
        this.maPN = maPN;
    }

    public PhieuNhap(String maPN, LocalDate ngayNhap, NhanVien maNV, NhaCungCap maNCC) {
        this.maPN = maPN;
        this.ngayNhap = ngayNhap;
        this.maNV = maNV;
        this.maNCC = maNCC;
    }

    public String getMaPN() {
        return maPN;
    }

    public void setMaPN(String maPN) {
        this.maPN = maPN;
    }

    public LocalDate getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(LocalDate ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public NhanVien getMaNV() {
        return maNV;
    }

    public void setMaNV(NhanVien maNV) {
        this.maNV = maNV;
    }

    public NhaCungCap getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(NhaCungCap maNCC) {
        this.maNCC = maNCC;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maPN == null) ? 0 : maPN.hashCode());
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
        PhieuNhap other = (PhieuNhap) obj;
        if (maPN == null) {
            if (other.maPN != null)
                return false;
        } else if (!maPN.equals(other.maPN))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PhieuNhap [maPN=" + maPN + ", ngayNhap=" + ngayNhap + ", maNV=" + maNV + ", maNCC=" + maNCC + "]";
    }

}

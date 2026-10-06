package entity;

import java.time.LocalDate;

public class LichLamViec {
    private String maLichLamViec;
    private LocalDate ngayLamViec;
    private NhanVien maNV;
    private CaLamViec maCa;

    public LichLamViec(String maLichLamViec, LocalDate ngayLamViec, NhanVien maNV, CaLamViec maCa) {
        this.maLichLamViec = maLichLamViec;
        this.ngayLamViec = ngayLamViec;
        this.maNV = maNV;
        this.maCa = maCa;
    }

    public LichLamViec(String maLichLamViec) {
        this.maLichLamViec = maLichLamViec;
    }

    public LichLamViec() {
    }

    public String getMaLichLamViec() {
        return maLichLamViec;
    }

    public void setMaLichLamViec(String maLichLamViec) {
        this.maLichLamViec = maLichLamViec;
    }

    public LocalDate getNgayLamViec() {
        return ngayLamViec;
    }

    public void setNgayLamViec(LocalDate ngayLamViec) {
        this.ngayLamViec = ngayLamViec;
    }

    public NhanVien getMaNV() {
        return maNV;
    }

    public void setMaNV(NhanVien maNV) {
        this.maNV = maNV;
    }

    public CaLamViec getMaCa() {
        return maCa;
    }

    public void setMaCa(CaLamViec maCa) {
        this.maCa = maCa;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maLichLamViec == null) ? 0 : maLichLamViec.hashCode());
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
        LichLamViec other = (LichLamViec) obj;
        if (maLichLamViec == null) {
            if (other.maLichLamViec != null)
                return false;
        } else if (!maLichLamViec.equals(other.maLichLamViec))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LichLamViec [maLichLamViec=" + maLichLamViec + ", ngayLamViec=" + ngayLamViec + ", maNV=" + maNV
                + ", maCa=" + maCa + "]";
    }

}

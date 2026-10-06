package entity;

import java.time.LocalTime;

public class CaLamViec {
    private String maCa;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;

    public CaLamViec(String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public CaLamViec(String maCa) {
        this.maCa = maCa;
    }

    public CaLamViec() {
    }

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        this.maCa = maCa;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maCa == null) ? 0 : maCa.hashCode());
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
        CaLamViec other = (CaLamViec) obj;
        if (maCa == null) {
            if (other.maCa != null)
                return false;
        } else if (!maCa.equals(other.maCa))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CaLamViec [maCa=" + maCa + ", tenCa=" + tenCa + ", gioBatDau=" + gioBatDau + ", gioKetThuc="
                + gioKetThuc + "]";
    }

}

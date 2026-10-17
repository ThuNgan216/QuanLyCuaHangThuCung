package entity;

public class SanPham {
    private String maSP;
    private String tenSP;
    private float giaBan;
    private int soLuongTon;

    public SanPham(String maSP, String tenSP, float giaBan, int soLuongTon) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.giaBan = giaBan;
        this.soLuongTon = soLuongTon;
    }

    public SanPham() {
    }

    public SanPham(String maSP) {
        this.maSP = maSP;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public String getTenSP() {
        return tenSP;
    }

    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }

    public float getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(float giaBan) {
        this.giaBan = giaBan;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maSP == null) ? 0 : maSP.hashCode());
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
        SanPham other = (SanPham) obj;
        if (maSP == null) {
            if (other.maSP != null)
                return false;
        } else if (!maSP.equals(other.maSP))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SanPham [maSP=" + maSP + ", tenSP=" + tenSP + ", giaBan=" + giaBan + ", soLuongTon=" + soLuongTon + "]";
    }

}

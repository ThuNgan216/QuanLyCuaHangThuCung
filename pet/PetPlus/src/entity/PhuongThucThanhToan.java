package entity;

public class PhuongThucThanhToan {
    private String maPTTT;
    private String tenPTTT;

    public PhuongThucThanhToan(String maPTTT, String tenPTTT) {
        this.maPTTT = maPTTT;
        this.tenPTTT = tenPTTT;
    }

    public PhuongThucThanhToan() {
    }

    public PhuongThucThanhToan(String maPTTT) {
        this.maPTTT = maPTTT;
    }

    public String getMaPTTT() {
        return maPTTT;
    }

    public void setMaPTTT(String maPTTT) {
        this.maPTTT = maPTTT;
    }

    public String getTenPTTT() {
        return tenPTTT;
    }

    public void setTenPTTT(String tenPTTT) {
        this.tenPTTT = tenPTTT;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maPTTT == null) ? 0 : maPTTT.hashCode());
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
        PhuongThucThanhToan other = (PhuongThucThanhToan) obj;
        if (maPTTT == null) {
            if (other.maPTTT != null)
                return false;
        } else if (!maPTTT.equals(other.maPTTT))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PhuongThucThanhToan [maPTTT=" + maPTTT + ", tenPTTT=" + tenPTTT + "]";
    }

}

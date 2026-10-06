package entity;

public class MaVach {
    private String maVach;
    private SanPham maSP;

    public MaVach() {
    }

    public MaVach(String maVach) {
        this.maVach = maVach;
    }

    public MaVach(String maVach, SanPham maSP) {
        this.maVach = maVach;
        this.maSP = maSP;
    }

    public String getMaVach() {
        return maVach;
    }

    public void setMaVach(String maVach) {
        this.maVach = maVach;
    }

    public SanPham getMaSP() {
        return maSP;
    }

    public void setMaSP(SanPham maSP) {
        this.maSP = maSP;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maVach == null) ? 0 : maVach.hashCode());
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
        MaVach other = (MaVach) obj;
        if (maVach == null) {
            if (other.maVach != null)
                return false;
        } else if (!maVach.equals(other.maVach))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MaVach [maVach=" + maVach + ", maSP=" + maSP + "]";
    }

}

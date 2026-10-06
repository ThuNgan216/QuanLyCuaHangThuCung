package entity;

public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String soDienThoai;
    private String diaChi;
    private String email;

    public NhaCungCap() {
    }

    public NhaCungCap(String maNCC) {
        this.maNCC = maNCC;
    }

    public NhaCungCap(String maNCC, String tenNCC, String soDienThoai, String diaChi, String email) {
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.email = email;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String maNCC) {
        this.maNCC = maNCC;
    }

    public String getTenNCC() {
        return tenNCC;
    }

    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maNCC == null) ? 0 : maNCC.hashCode());
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
        NhaCungCap other = (NhaCungCap) obj;
        if (maNCC == null) {
            if (other.maNCC != null)
                return false;
        } else if (!maNCC.equals(other.maNCC))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NhaCungCap [maNCC=" + maNCC + ", tenNCC=" + tenNCC + ", soDienThoai=" + soDienThoai + ", diaChi="
                + diaChi + ", email=" + email + "]";
    }

}

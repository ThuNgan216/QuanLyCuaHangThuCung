package entity;

public class NhanVien {
    private String maNV;
    private String tenNV;
    private String sdt;
    private String cccd;
    private String diaChi;
    private String email;
    private String chucVu;
    private TaiKhoan maTK;

    public NhanVien() {
    }

    public NhanVien(String maNV) {
        this.maNV = maNV;
    }

    public NhanVien(String maNV, String tenNV, String sdt, String cccd, String diaChi, String email, String chucVu,
            TaiKhoan maTK) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.sdt = sdt;
        this.cccd = cccd;
        this.diaChi = diaChi;
        this.email = email;
        this.chucVu = chucVu;
        this.maTK = maTK;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
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

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public TaiKhoan getMaTK() {
        return maTK;
    }

    public void setMaTK(TaiKhoan maTK) {
        this.maTK = maTK;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maNV == null) ? 0 : maNV.hashCode());
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
        NhanVien other = (NhanVien) obj;
        if (maNV == null) {
            if (other.maNV != null)
                return false;
        } else if (!maNV.equals(other.maNV))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NhanVien [maNV=" + maNV + ", tenNV=" + tenNV + ", sdt=" + sdt + ", cccd=" + cccd + ", diaChi=" + diaChi
                + ", email=" + email + ", chucVu=" + chucVu + ", maTK=" + maTK + "]";
    }

}

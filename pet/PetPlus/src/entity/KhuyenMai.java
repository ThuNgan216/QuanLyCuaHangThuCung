package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class KhuyenMai {
    public static final String PHAM_VI_HOA_DON = "Tổng hóa đơn";
    public static final String PHAM_VI_SAN_PHAM = "Sản phẩm";
    private static final String DAU_CAU_HINH = "[PETPLUS-KM1|";

    private String maKhuyenMai;
    private String tenKhuyenMai;
    private float phanTramGiam;
    private String dieuKienApDung;
    private String phamViApDung = PHAM_VI_HOA_DON;
    /** 0 nghĩa là tất cả hạng khách hàng. */
    private int hangKhachHangApDung;
    private int soLuongToiThieu = 1;
    private List<String> maSanPhamApDung = new ArrayList<>();
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;

    public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, float phanTramGiam, String dieuKienApDung,
            LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.phanTramGiam = phanTramGiam;
        setDieuKienApDung(dieuKienApDung);
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public KhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public KhuyenMai() {
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getTenKhuyenMai() {
        return tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }

    public float getPhanTramGiam() {
        return phanTramGiam;
    }

    public void setPhanTramGiam(float phanTramGiam) {
        this.phanTramGiam = phanTramGiam;
    }

    public String getDieuKienApDung() {
        return dieuKienApDung;
    }

    public void setDieuKienApDung(String dieuKienApDung) {
        phamViApDung = PHAM_VI_HOA_DON;
        hangKhachHangApDung = 0;
        soLuongToiThieu = 1;
        maSanPhamApDung = new ArrayList<>();
        if (dieuKienApDung == null || !dieuKienApDung.startsWith(DAU_CAU_HINH)) {
            this.dieuKienApDung = dieuKienApDung;
            return;
        }

        int ketThuc = dieuKienApDung.indexOf(']', DAU_CAU_HINH.length());
        if (ketThuc < 0) {
            this.dieuKienApDung = dieuKienApDung;
            return;
        }
        String[] cauHinh = dieuKienApDung.substring(DAU_CAU_HINH.length(), ketThuc).split("\\|", -1);
        if (cauHinh.length < 2) {
            this.dieuKienApDung = dieuKienApDung;
            return;
        }
        if ("PRODUCT".equals(cauHinh[0])) phamViApDung = PHAM_VI_SAN_PHAM;
        try {
            int hang = Integer.parseInt(cauHinh[1]);
            hangKhachHangApDung = hang >= 1 && hang <= 3 ? hang : 0;
        } catch (NumberFormatException ignored) {
            hangKhachHangApDung = 0;
        }
        if (cauHinh.length >= 3) {
            try {
                soLuongToiThieu = Math.max(1, Integer.parseInt(cauHinh[2]));
            } catch (NumberFormatException ignored) {
                soLuongToiThieu = 1;
            }
        }
        if (cauHinh.length >= 4 && !cauHinh[3].trim().isEmpty()) {
            setMaSanPhamApDung(java.util.Arrays.asList(cauHinh[3].split(",")));
        }
        this.dieuKienApDung = dieuKienApDung.substring(ketThuc + 1).trim();
    }

    /** Mã hóa loại, hạng, số lượng và sản phẩm vào cột điều kiện hiện có. */
    public String getDieuKienApDungLuu() {
        String loai = PHAM_VI_SAN_PHAM.equals(phamViApDung) ? "PRODUCT" : "BILL";
        String dieuKien = dieuKienApDung == null ? "" : dieuKienApDung.trim();
        String cauHinh = DAU_CAU_HINH + loai + "|" + hangKhachHangApDung;
        if (PHAM_VI_SAN_PHAM.equals(phamViApDung)) {
            cauHinh += "|" + Math.max(1, soLuongToiThieu) + "|" + String.join(",", maSanPhamApDung);
        }
        return cauHinh + "]"
                + (dieuKien.isEmpty() ? "" : " " + dieuKien);
    }

    public String getPhamViApDung() {
        return phamViApDung;
    }

    public void setPhamViApDung(String phamViApDung) {
        this.phamViApDung = PHAM_VI_SAN_PHAM.equals(phamViApDung) ? PHAM_VI_SAN_PHAM : PHAM_VI_HOA_DON;
    }

    public int getHangKhachHangApDung() {
        return hangKhachHangApDung;
    }

    public void setHangKhachHangApDung(int hangKhachHangApDung) {
        this.hangKhachHangApDung = hangKhachHangApDung >= 1 && hangKhachHangApDung <= 3
                ? hangKhachHangApDung : 0;
    }

    public int getSoLuongToiThieu() {
        return soLuongToiThieu;
    }

    public void setSoLuongToiThieu(int soLuongToiThieu) {
        this.soLuongToiThieu = Math.max(1, soLuongToiThieu);
    }

    public List<String> getMaSanPhamApDung() {
        return new ArrayList<>(maSanPhamApDung);
    }

    public void setMaSanPhamApDung(List<String> maSanPhamApDung) {
        LinkedHashSet<String> maKhongTrung = new LinkedHashSet<>();
        if (maSanPhamApDung != null) {
            for (String maSP : maSanPhamApDung) {
                if (maSP != null && !maSP.trim().isEmpty()) maKhongTrung.add(maSP.trim());
            }
        }
        this.maSanPhamApDung = new ArrayList<>(maKhongTrung);
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maKhuyenMai == null) ? 0 : maKhuyenMai.hashCode());
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
        KhuyenMai other = (KhuyenMai) obj;
        if (maKhuyenMai == null) {
            if (other.maKhuyenMai != null)
                return false;
        } else if (!maKhuyenMai.equals(other.maKhuyenMai))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KhuyenMai [maKhuyenMai=" + maKhuyenMai + ", tenKhuyenMai=" + tenKhuyenMai + ", phanTramGiam="
                + phanTramGiam + ", dieuKienApDung=" + dieuKienApDung + ", phamViApDung=" + phamViApDung
                + ", hangKhachHangApDung=" + hangKhachHangApDung + ", soLuongToiThieu=" + soLuongToiThieu
                + ", maSanPhamApDung=" + maSanPhamApDung + ", ngayBatDau=" + ngayBatDau
                + ", ngayKetThuc=" + ngayKetThuc + "]";
    }

}

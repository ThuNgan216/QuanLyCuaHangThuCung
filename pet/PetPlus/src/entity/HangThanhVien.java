package entity;

/**
 * Hạng thành viên của khách hàng, tính từ điểm tích lũy.
 * Quy tắc: cứ đủ {@value #DIEM_MOI_HANG} điểm thì tăng 1 hạng, chỉ có 3 bậc 1, 2, 3 (hạng 3 là cao nhất).
 * Cột KhachHang.hangThanhVien trong CSDL lưu {@link #getMa()}.
 * Điểm kiếm được theo quy tắc 1 điểm / 10.000 đ (xem ThanhToanDAO.capNhatDiem).
 */
public enum HangThanhVien {
    HANG_1(1),
    HANG_2(2),
    HANG_3(3);

    /** Số điểm cần có để tăng thêm 1 hạng. */
    public static final int DIEM_MOI_HANG = 2000;

    private final int ma;

    HangThanhVien(int ma) {
        this.ma = ma;
    }

    /** Mã hạng lưu trong CSDL: 1, 2 hoặc 3. */
    public int getMa() { return ma; }

    public String getTen() { return "Hạng " + ma; }

    /** Số điểm tối thiểu để đạt hạng này: hạng 1 từ 0, hạng 2 từ 2000, hạng 3 từ 4000. */
    public int getDiemToiThieu() { return (ma - 1) * DIEM_MOI_HANG; }

    /** Hạng ứng với số điểm: mỗi 2000 điểm tăng 1 hạng, tối đa hạng 3. */
    public static HangThanhVien tuDiem(int diem) {
        return tuMa(Math.max(0, diem) / DIEM_MOI_HANG + 1);
    }

    /** Đổi mã trong CSDL sang hạng; mã trống (NULL = 0) coi là hạng 1, mã lớn hơn 3 coi là hạng 3. */
    public static HangThanhVien tuMa(int ma) {
        HangThanhVien[] ds = values();
        return ds[Math.min(Math.max(ma, 1), ds.length) - 1];
    }

    /** Hạng cao hơn liền kề, null nếu đã ở hạng cao nhất. */
    public HangThanhVien hangTiepTheo() {
        HangThanhVien[] ds = values();
        return ordinal() + 1 < ds.length ? ds[ordinal() + 1] : null;
    }

    /** Số điểm còn thiếu để lên hạng kế tiếp; 0 nếu đã ở hạng cao nhất. */
    public int diemConThieu(int diemHienTai) {
        HangThanhVien tiep = hangTiepTheo();
        return tiep == null ? 0 : Math.max(0, tiep.getDiemToiThieu() - diemHienTai);
    }

    @Override
    public String toString() { return getTen(); }
}

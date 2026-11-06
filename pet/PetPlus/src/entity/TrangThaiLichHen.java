package entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum TrangThaiLichHen {
    CHO_XAC_NHAN("Chờ xác nhận"),
    DA_XAC_NHAN("Đã xác nhận"),
    DA_TIEP_NHAN("Đã tiếp nhận"),
    DANG_THUC_HIEN("Đang thực hiện"),
    HOAN_THANH("Hoàn thành"),
    DA_HUY("Đã hủy");

    private final String tenHienThi;

    TrangThaiLichHen(String tenHienThi) { this.tenHienThi = tenHienThi; }

    public String getTenHienThi() { return tenHienThi; }

    @Override public String toString() { return tenHienThi; }

    /** Trả về null nếu không nhận ra, để nơi gọi tự xử lý (không âm thầm đổi thành "Chờ xác nhận"). */
    public static TrangThaiLichHen tuTen(String ten) {
        if (ten == null) return null;
        String t = ten.trim();
        for (TrangThaiLichHen tt : values())
            if (tt.tenHienThi.equalsIgnoreCase(t) || tt.name().equalsIgnoreCase(t)) return tt;
        return null;
    }

    public static boolean laTrangThaiQuanLyHopLe(String ten) { return tuTen(ten) != null; }

    /** Trạng thái do module tiếp nhận tự đặt, người dùng không đổi tay ở màn hình lịch hẹn. */
    public boolean doTiepNhanQuanLy() { return this == DA_TIEP_NHAN || this == DANG_THUC_HIEN; }

    /** Các trạng thái cho phép chọn tay trong form lịch hẹn. */
    public static TrangThaiLichHen[] chonDuoc() {
        return new TrangThaiLichHen[] { CHO_XAC_NHAN, DA_XAC_NHAN, HOAN_THANH, DA_HUY };
    }

    /** Lịch đã kết thúc hoặc bị hủy: không còn chiếm khung giờ của nhân viên. */
    public boolean giuKhungGio() { return this != DA_HUY && this != HOAN_THANH; }

    /** Trạng thái cuối: không được sửa hay chuyển đi đâu nữa. */
    public boolean laTrangThaiCuoi() { return this == HOAN_THANH || this == DA_HUY; }

    /** Lịch hẹn chưa kết thúc (còn hiệu lực) – dùng để chặn xóa/đổi chủ khách hàng, thú cưng. */
    public boolean chuaKetThuc() { return !laTrangThaiCuoi(); }

    /**
     * Máy trạng thái của lịch hẹn:
     * Chờ xác nhận → Đã xác nhận → Đã tiếp nhận → Đang thực hiện → Hoàn thành,
     * và hủy được khi chưa tiếp nhận. Hoàn thành / Đã hủy là trạng thái cuối.
     * Chuyển sang Đã tiếp nhận / Đang thực hiện do màn hình Tiếp nhận thực hiện.
     */
    public boolean coTheChuyenSang(TrangThaiLichHen moi) {
        if (moi == null) return false;
        if (this == moi) return true;
        switch (this) {
            case CHO_XAC_NHAN:   return moi == DA_XAC_NHAN || moi == DA_HUY;
            case DA_XAC_NHAN:    return moi == DA_TIEP_NHAN || moi == HOAN_THANH || moi == DA_HUY;
            case DA_TIEP_NHAN:   return moi == DANG_THUC_HIEN || moi == HOAN_THANH;
            case DANG_THUC_HIEN: return moi == HOAN_THANH;
            default:             return false;
        }
    }

    /** Trạng thái được phép chọn khi tạo lịch hẹn mới. */
    public static TrangThaiLichHen[] chonDuocKhiTaoMoi() {
        return new TrangThaiLichHen[] { CHO_XAC_NHAN, DA_XAC_NHAN };
    }

    /** Trạng thái người dùng được chọn tay khi sửa lịch đang ở trạng thái này (gồm cả chính nó). */
    public List<TrangThaiLichHen> luaChonThuCong() {
        List<TrangThaiLichHen> ds = new ArrayList<>();
        ds.add(this);
        for (TrangThaiLichHen t : chonDuoc())
            if (t != this && coTheChuyenSang(t)) ds.add(t);
        return ds;
    }
}

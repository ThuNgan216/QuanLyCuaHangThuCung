package entity;

import java.util.Arrays;

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
}

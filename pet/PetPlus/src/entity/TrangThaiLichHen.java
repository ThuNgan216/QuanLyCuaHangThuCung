package entity;

public enum TrangThaiLichHen {
    CHO_XAC_NHAN("Chờ xác nhận"), DA_XAC_NHAN("Đã xác nhận"), DA_TIEP_NHAN("Đã tiếp nhận"),
    DANG_THUC_HIEN("Đang thực hiện"), HOAN_THANH("Hoàn thành"), DA_HUY("Đã hủy");

    private final String tenHienThi;

    TrangThaiLichHen(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    @Override
    public String toString() {
        return tenHienThi;
    }

    public static TrangThaiLichHen tuTen(String ten) {
        if (ten != null)
            for (TrangThaiLichHen tt : values())
                if (tt.tenHienThi.equalsIgnoreCase(ten.trim()) || tt.name().equalsIgnoreCase(ten.trim()))
                    return tt;
        return CHO_XAC_NHAN;
    }
}

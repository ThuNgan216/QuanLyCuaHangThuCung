package entity;

public enum TrangThaiLichHen {
    CHO_XAC_NHAN("Chờ xác nhận"), DA_XAC_NHAN("Đã xác nhận"), DA_HUY("Đã hủy");
    private final String tenHienThi;
    TrangThaiLichHen(String tenHienThi) { this.tenHienThi = tenHienThi; }
    public String getTenHienThi() { return tenHienThi; }
    @Override public String toString() { return tenHienThi; }
    public static TrangThaiLichHen tuTen(String ten) {
        if (ten != null) for (TrangThaiLichHen tt : values()) if (tt.tenHienThi.equalsIgnoreCase(ten.trim()) || tt.name().equalsIgnoreCase(ten.trim())) return tt;
        return CHO_XAC_NHAN;
    }

    public static boolean laTrangThaiQuanLyHopLe(String ten) {
        return ten != null && java.util.Arrays.stream(values())
                .anyMatch(tt -> tt.tenHienThi.equalsIgnoreCase(ten.trim()) || tt.name().equalsIgnoreCase(ten.trim()));
    }
}

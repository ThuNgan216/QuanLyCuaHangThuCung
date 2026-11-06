package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Các xử lý giống nhau của DAO khách hàng, thú cưng, lịch hẹn, dịch vụ được gom về đây
 * để cả 4 module chạy cùng một cách: sinh mã tự động, nhận biết lỗi trùng khóa, số lần thử lại.
 */
final class TienIchDAO {
    private TienIchDAO() {
    }

    /** Số lần thử lại tối đa khi hai người thêm cùng lúc và sinh trùng mã. */
    static final int SO_LAN_THU_TOI_DA = 3;

    /** Điều kiện SQL cho lịch hẹn không còn hiệu lực (đã kết thúc, đã hủy hoặc đã xóa). */
    static final String LICH_HEN_KHONG_CON_HIEU_LUC = "(N'Hoàn thành', N'Đã hủy', N'Đã xóa')";

    /**
     * Mã tiếp theo theo số thứ tự lớn nhất hiện có (kể cả bản ghi đã ẩn, để không dùng lại mã cũ),
     * định dạng tiền tố + 3 chữ số: KH001, TC001, LH001, DV001...
     * Tên bảng, cột và tiền tố chỉ truyền vào từ hằng số trong DAO, không lấy từ người dùng.
     */
    static String sinhMa(Connection con, String bang, String cot, String tienTo) throws SQLException {
        int doDaiTienTo = tienTo.length();
        String sql = "SELECT MAX(TRY_CONVERT(INT, SUBSTRING(" + cot + ", " + (doDaiTienTo + 1) + ", 18))) FROM "
                + bang + " WHERE " + cot + " LIKE '" + tienTo + "[0-9]%'";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int max = rs.next() ? rs.getInt(1) : 0;
            return String.format(Locale.ROOT, "%s%03d", tienTo, max + 1);
        }
    }

    /** Lỗi trùng khóa chính / unique của một ràng buộc cụ thể (2627: constraint, 2601: unique index). */
    static boolean laTrungRangBuoc(SQLException e, String tenRangBuoc) {
        for (SQLException cur = e; cur != null; cur = cur.getNextException()) {
            if ((cur.getErrorCode() == 2627 || cur.getErrorCode() == 2601) && cur.getMessage() != null
                    && cur.getMessage().contains(tenRangBuoc))
                return true;
        }
        return false;
    }
}

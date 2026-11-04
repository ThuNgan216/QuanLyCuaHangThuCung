package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Transactional hard delete when unreferenced, otherwise mark the row hidden. */
final class DeletePolicy {
    private DeletePolicy() {
    }

    static boolean xoaHoacAn(Connection connection, String sqlKhoaBanGhi,
            String[] sqlKiemTraThamChieu, String sqlAn, String sqlXoa, String ma)
            throws SQLException {
        boolean autoCommitCu = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            if (!tonTai(connection, sqlKhoaBanGhi, ma)) {
                connection.commit();
                return false;
            }

            boolean dangDuocThamChieu = false;
            for (String sql : sqlKiemTraThamChieu) {
                if (tonTai(connection, sql, ma)) {
                    dangDuocThamChieu = true;
                    break;
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    dangDuocThamChieu ? sqlAn : sqlXoa)) {
                ps.setString(1, ma);
                boolean daXuLy = ps.executeUpdate() > 0;
                connection.commit();
                return daXuLy;
            }
        } catch (SQLException | RuntimeException ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                ex.addSuppressed(rollbackError);
            }
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommitCu);
        }
    }

    private static boolean tonTai(Connection connection, String sql, String ma)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}

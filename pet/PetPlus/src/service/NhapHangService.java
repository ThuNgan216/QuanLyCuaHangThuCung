package service;

import dao.ChiTietPhieuNhapDAO;
import dao.PhieuNhapDAO;
import dao.SanPhamDAO;
import database.ConnectDB;
import entity.ChiTietPhieuNhap;
import entity.PhieuNhap;
import entity.SanPham;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class NhapHangService {

    private final PhieuNhapDAO phieuNhapDAO = new PhieuNhapDAO();
    private final ChiTietPhieuNhapDAO chiTietPhieuNhapDAO = new ChiTietPhieuNhapDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    // Lấy toàn bộ danh sách phiếu nhập
    public List<PhieuNhap> layDanhSachPhieuNhap() {
        return phieuNhapDAO.layDanhSach();
    }
    
    // Lấy chi tiết phiếu nhập
    public List<ChiTietPhieuNhap> layChiTiet(String maPN) throws SQLException {
        if (maPN == null || maPN.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu nhập không hợp lệ.");
        }
        return chiTietPhieuNhapDAO.layChiTietTheoMaPN(maPN);
    }

    /**
     * Thực hiện Transaction Nhập hàng
     * Gồm 3 bước: Thêm Phiếu Nhập -> Thêm Chi Tiết -> Cập nhật Số lượng tồn
     */
    public boolean thucHienNhapHang(PhieuNhap phieuNhap, List<ChiTietPhieuNhap> danhSachChiTiet) {
        if (phieuNhap == null || danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu phiếu nhập hoặc chi tiết không hợp lệ.");
        }

        Connection con;
        try {
            con = ConnectDB.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối CSDL.", e);
        }
        if (con == null) {
            throw new RuntimeException("Không thể kết nối CSDL.");
        }

        try {
            // Bắt đầu Transaction
            con.setAutoCommit(false);

            // 1. Lưu phiếu nhập
            if (!phieuNhapDAO.them(phieuNhap, con)) {
                throw new SQLException("Không thể lưu Phiếu Nhập (Mã: " + phieuNhap.getMaPN() + ")");
            }

            // 2. Lưu từng chi tiết phiếu nhập & 3. Cập nhật tồn kho
            for (ChiTietPhieuNhap ct : danhSachChiTiet) {
                // Lưu chi tiết
                if (!chiTietPhieuNhapDAO.them(ct, con)) {
                    throw new SQLException("Không thể lưu Chi tiết cho sản phẩm: " + ct.getMaSP().getMaSP());
                }

                // Cập nhật tồn kho (Cộng dồn)
                SanPham spCu = sanPhamDAO.timTheoMa(ct.getMaSP().getMaSP());
                if (spCu == null) {
                    throw new SQLException("Sản phẩm không tồn tại trong kho: " + ct.getMaSP().getMaSP());
                }

                int tonKhoMoi = spCu.getSoLuongTon() + ct.getSoLuongNhap();
                spCu.setSoLuongTon(tonKhoMoi);
                
                // Do SanPhamDAO.capNhat() không có hàm truyền Connection, 
                // ta sẽ update tồn kho trực tiếp trong Transaction này để an toàn
                String sqlUpdateTonKho = "UPDATE SanPham SET soLuongTon = ? WHERE maSP = ?";
                try (java.sql.PreparedStatement psTonKho = con.prepareStatement(sqlUpdateTonKho)) {
                    psTonKho.setInt(1, tonKhoMoi);
                    psTonKho.setString(2, spCu.getMaSP());
                    if (psTonKho.executeUpdate() <= 0) {
                        throw new SQLException("Lỗi cập nhật tồn kho cho sản phẩm: " + spCu.getMaSP());
                    }
                }
            }

            // Nếu mọi thứ OK -> Commit lưu vào DB
            con.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            // Nếu có lỗi ở bất kỳ bước nào -> Rollback hủy toàn bộ thay đổi
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Lỗi nhập hàng: " + e.getMessage());
        } finally {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

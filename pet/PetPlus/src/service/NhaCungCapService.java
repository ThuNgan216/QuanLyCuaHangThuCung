package service;

import dao.NhaCungCapDAO;
import entity.NhaCungCap;
import java.util.List;
import java.util.stream.Collectors;

public class NhaCungCapService {

    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();

    // Lấy toàn bộ danh sách nhà cung cấp
    public List<NhaCungCap> layDanhSach() {
        return nhaCungCapDAO.layDanhSach();
    }

    // Tìm nhà cung cấp theo mã
    public NhaCungCap timTheoMa(String maNCC) {
        if (maNCC == null || maNCC.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không được để trống.");
        }
        return nhaCungCapDAO.timTheoMa(maNCC.trim());
    }

// Tra cứu / tìm kiếm nhà cung cấp theo Mã, Tên hoặc SĐT
public List<NhaCungCap> timKiem(String tuKhoa) {
    if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
        return layDanhSach();
    }
    String lowerKeyword = tuKhoa.trim().toLowerCase();
    return layDanhSach().stream()
            .filter(ncc -> (ncc.getMaNCC() != null && ncc.getMaNCC().toLowerCase().contains(lowerKeyword))
                    || (ncc.getTenNCC() != null && ncc.getTenNCC().toLowerCase().contains(lowerKeyword))
                    || (ncc.getSoDienThoai() != null && ncc.getSoDienThoai().contains(lowerKeyword)))
            .collect(Collectors.toList());
}

    // Thêm nhà cung cấp mới
    public boolean them(NhaCungCap ncc) {
        kiemTraDuLieu(ncc);

        if (nhaCungCapDAO.timTheoMa(ncc.getMaNCC().trim()) != null) {
            throw new IllegalArgumentException("Mã nhà cung cấp đã tồn tại trong hệ thống.");
        }

        return nhaCungCapDAO.them(ncc);
    }

    // Cập nhật nhà cung cấp
    public boolean capNhat(NhaCungCap ncc) {
        kiemTraDuLieu(ncc);

        if (nhaCungCapDAO.timTheoMa(ncc.getMaNCC().trim()) == null) {
            throw new IllegalArgumentException("Nhà cung cấp không tồn tại.");
        }

        return nhaCungCapDAO.capNhat(ncc);
    }

    // Xóa nhà cung cấp
    public boolean xoa(String maNCC) {
        if (maNCC == null || maNCC.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không được để trống.");
        }

        if (nhaCungCapDAO.timTheoMa(maNCC.trim()) == null) {
            throw new IllegalArgumentException("Nhà cung cấp không tồn tại.");
        }

        return nhaCungCapDAO.xoa(maNCC.trim());
    }

    // Validate dữ liệu nghiệp vụ
    private void kiemTraDuLieu(NhaCungCap ncc) {
        if (ncc == null) {
            throw new IllegalArgumentException("Thông tin nhà cung cấp không hợp lệ.");
        }

        String ma = ncc.getMaNCC() == null ? "" : ncc.getMaNCC().trim();
        String ten = ncc.getTenNCC() == null ? "" : ncc.getTenNCC().trim();
        String sdt = ncc.getSoDienThoai() == null ? "" : ncc.getSoDienThoai().trim();
        String diaChi = ncc.getDiaChi() == null ? "" : ncc.getDiaChi().trim();
        String email = ncc.getEmail() == null ? "" : ncc.getEmail().trim();

        if (ma.isEmpty()) throw new IllegalArgumentException("Mã nhà cung cấp không được để trống.");
        if (ma.length() > 20) throw new IllegalArgumentException("Mã nhà cung cấp tối đa 20 ký tự.");

        if (ten.isEmpty()) throw new IllegalArgumentException("Tên nhà cung cấp không được để trống.");
        if (ten.length() > 150) throw new IllegalArgumentException("Tên nhà cung cấp tối đa 150 ký tự.");

        if (!sdt.isEmpty() && !sdt.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.");
        }

        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email không đúng định dạng chuẩn.");
        }

        ncc.setMaNCC(ma);
        ncc.setTenNCC(ten);
        ncc.setSoDienThoai(sdt.isEmpty() ? null : sdt);
        ncc.setDiaChi(diaChi.isEmpty() ? null : diaChi);
        ncc.setEmail(email.isEmpty() ? null : email);
    }
}
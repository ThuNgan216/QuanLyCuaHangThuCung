package service;

import dao.KhachHangDAO;
import dao.LichHenDAO;
import dao.ThuCungDAO;
import entity.KhachHang;
import entity.LichHen;
import entity.NhanVien;
import entity.ThuCung;
import entity.TrangThaiLichHen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Nghiệp vụ lịch hẹn:
 * <ul>
 * <li>Trạng thái đi theo máy trạng thái ({@link TrangThaiLichHen#coTheChuyenSang}).</li>
 * <li>Khách hàng, thú cưng, nhân viên phải tồn tại; thú cưng phải thuộc khách hàng đã chọn;
 * nhân viên phải là nhân viên chăm sóc đang hoạt động.</li>
 * <li>Lịch nằm trong ca làm việc của nhân viên (bảng LichLamViec / CaLamViec).</li>
 * <li>Mỗi lịch kéo dài {@value #DO_DAI_LICH_PHUT} phút; nhân viên và thú cưng không bị đặt chồng giờ.</li>
 * <li>Lịch đã tiếp nhận / hoàn thành / hủy không được sửa; chỉ xóa lịch Chờ xác nhận hoặc Đã hủy.</li>
 * </ul>
 */
public class LichHenService {
    /** Mỗi lịch hẹn chiếm 30 phút của nhân viên. */
    public static final int DO_DAI_LICH_PHUT = 30;

    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final LichHenDAO dao = new LichHenDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final ThuCungDAO thuCungDAO = new ThuCungDAO();

    // ---------------------------------------------------------------- truy vấn

    public List<LichHen> layDanhSach() {
        return dao.layDanhSach();
    }

    /** Tìm theo mã lịch, tên / SĐT khách hàng, tên thú cưng, tên nhân viên hoặc trạng thái. */
    public List<LichHen> timKiem(String tuKhoa) {
        String key = KiemTra.trim(tuKhoa);
        return key.isEmpty() ? layDanhSach() : dao.timKiem(key);
    }

    public LichHen timTheoMa(String ma) {
        return dao.timTheoMa(KiemTra.maBatBuoc(ma, "lịch hẹn"));
    }

    public List<NhanVien> layNhanVien() {
        return dao.layNhanVien();
    }

    public List<KhachHang> timKhachHang(String key) {
        return dao.timKhachHang(key);
    }

    public List<ThuCung> layThuCungTheoKhach(String maKH, String key) {
        return dao.layThuCungTheoKhach(maKH, key);
    }

    /** Mã gợi ý hiển thị trên form; mã thật do DAO sinh khi lưu. */
    public String taoMaMoi() {
        return dao.taoMaMoi();
    }

    /** Các trạng thái được chọn trên form: lịch mới (cu = null) hoặc theo trạng thái hiện tại của lịch đang sửa. */
    public List<TrangThaiLichHen> layTrangThaiChoPhep(LichHen cu) {
        if (cu == null)
            return Arrays.asList(TrangThaiLichHen.chonDuocKhiTaoMoi());
        TrangThaiLichHen tt = TrangThaiLichHen.tuTen(cu.getTrangThai());
        return tt == null ? new ArrayList<TrangThaiLichHen>() : tt.luaChonThuCong();
    }

    /**
     * Nhân viên có thể nhận lịch vào đúng giờ này: đang hoạt động, thuộc bộ phận chăm sóc, ca làm việc phủ kín
     * khoảng [giờ hẹn, giờ hẹn + 30 phút] và chưa có lịch khác chồng giờ. Dùng cho form đặt lịch để người dùng
     * chỉ thấy những nhân viên chọn được, không phải chọn xong mới bị báo lỗi.
     *
     * @param maLichBoQua mã lịch đang sửa (không tính là "bận"), null khi tạo mới
     */
    public List<NhanVien> layNhanVienConTrong(LocalDateTime thoiGian, String maLichBoQua) {
        List<NhanVien> kq = new ArrayList<>();
        if (thoiGian == null)
            return kq;
        LocalDateTime ketThuc = thoiGian.plusMinutes(DO_DAI_LICH_PHUT);
        if (!ketThuc.toLocalDate().equals(thoiGian.toLocalDate()))
            return kq; // lịch không được kéo dài sang ngày hôm sau
        java.util.Map<String, List<LocalTime[]>> caTheoNV = dao.layCaLamTheoNgay(thoiGian.toLocalDate());
        java.util.Set<String> ban = dao.layMaNhanVienBan(thoiGian, DO_DAI_LICH_PHUT, maLichBoQua);
        for (NhanVien nv : dao.layNhanVien()) {
            List<LocalTime[]> ca = caTheoNV.get(nv.getMaNV());
            if (ca == null || ban.contains(nv.getMaNV()))
                continue;
            if (phuKhoangGio(gopCa(ca), thoiGian.toLocalTime(), ketThuc.toLocalTime()))
                kq.add(nv);
        }
        return kq;
    }

    /** Gộp các ca liền nhau hoặc chồng nhau (ví dụ 07:00–12:00 và 12:00–17:00 thành 07:00–17:00). */
    private static List<LocalTime[]> gopCa(List<LocalTime[]> caLam) {
        List<LocalTime[]> sapXep = new ArrayList<>(caLam);
        sapXep.sort((a, b) -> a[0].compareTo(b[0]));
        List<LocalTime[]> khoang = new ArrayList<>();
        for (LocalTime[] ca : sapXep) {
            LocalTime[] cuoi = khoang.isEmpty() ? null : khoang.get(khoang.size() - 1);
            if (cuoi == null || ca[0].isAfter(cuoi[1]))
                khoang.add(new LocalTime[] { ca[0], ca[1] });
            else if (ca[1].isAfter(cuoi[1]))
                cuoi[1] = ca[1];
        }
        return khoang;
    }

    private static boolean phuKhoangGio(List<LocalTime[]> khoang, LocalTime tu, LocalTime den) {
        for (LocalTime[] k : khoang)
            if (!tu.isBefore(k[0]) && !den.isAfter(k[1]))
                return true;
        return false;
    }

    /** Ném lỗi nếu lịch hẹn không còn được phép sửa (đang do màn hình Tiếp nhận quản lý, hoặc đã ở trạng thái cuối). */
    public void kiemTraChoPhepSua(LichHen cu) {
        if (cu == null)
            throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");
        TrangThaiLichHen tt = TrangThaiLichHen.tuTen(cu.getTrangThai());
        if (tt == null)
            throw new IllegalArgumentException("Trạng thái hiện tại của lịch hẹn không hợp lệ.");
        if (tt.doTiepNhanQuanLy())
            throw new IllegalArgumentException("Lịch hẹn " + cu.getMaLichHen() + " đang ở trạng thái \"" + tt
                    + "\" do màn hình Tiếp nhận quản lý nên không thể chỉnh sửa tại đây.");
        if (tt.laTrangThaiCuoi())
            throw new IllegalArgumentException("Lịch hẹn " + cu.getMaLichHen() + " đã \"" + tt.getTenHienThi()
                    + "\" (trạng thái cuối) nên không thể chỉnh sửa.");
    }

    // ---------------------------------------------------------------- thêm / sửa / đổi trạng thái / xóa

    public boolean them(LichHen l) {
        kiemTraDuLieuCoBan(l);
        TrangThaiLichHen tt = TrangThaiLichHen.tuTen(l.getTrangThai());
        if (!Arrays.asList(TrangThaiLichHen.chonDuocKhiTaoMoi()).contains(tt))
            throw new IllegalArgumentException("Lịch hẹn mới chỉ được tạo ở trạng thái \"Chờ xác nhận\" hoặc \"Đã xác nhận\".");
        if (!l.getThoiGianHen().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Thời gian hẹn phải ở tương lai, không được chọn thời điểm hiện tại hoặc đã qua.");
        kiemTraThamChieu(l);
        kiemTraCaLamViec(l);
        kiemTraTrungLich(l, null);
        // DAO sinh mã, khóa khung giờ và kiểm tra trùng lần nữa trong transaction (tránh hai người đặt cùng lúc).
        return dao.them(l, DO_DAI_LICH_PHUT);
    }

    public boolean capNhat(LichHen l) {
        kiemTraDuLieuCoBan(l);
        LichHen cu = KiemTra.phaiTonTai(dao.timTheoMa(l.getMaLichHen()), "lịch hẹn");
        kiemTraChoPhepSua(cu);

        TrangThaiLichHen ttCu = TrangThaiLichHen.tuTen(cu.getTrangThai());
        TrangThaiLichHen ttMoi = TrangThaiLichHen.tuTen(l.getTrangThai());
        if (!ttCu.coTheChuyenSang(ttMoi))
            throw new IllegalArgumentException("Không thể chuyển lịch hẹn từ \"" + ttCu + "\" sang \"" + ttMoi + "\".");

        boolean doiGio = !cu.getThoiGianHen().equals(l.getThoiGianHen());
        boolean doiNV = !cu.getMaNV().getMaNV().equalsIgnoreCase(l.getMaNV().getMaNV());
        boolean doiKhachHoacThuCung = !cu.getMaKH().getMaKH().equalsIgnoreCase(l.getMaKH().getMaKH())
                || !cu.getMaThuCung().getMaThuCung().equalsIgnoreCase(l.getMaThuCung().getMaThuCung());

        if (ttMoi == TrangThaiLichHen.HOAN_THANH) {
            if (doiGio || doiNV || doiKhachHoacThuCung)
                throw new IllegalArgumentException(
                        "Khi đánh dấu Hoàn thành không được đổi thời gian, nhân viên, khách hàng hoặc thú cưng.");
            kiemTraDaDenGioHen(cu.getThoiGianHen());
        } else if (doiGio && !l.getThoiGianHen().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn phải ở tương lai, không được chọn thời điểm hiện tại hoặc đã qua.");
        }

        kiemTraThamChieu(l);
        if (ttMoi.giuKhungGio()) {
            // Chỉ kiểm tra ca làm việc khi đổi giờ / nhân viên, để việc xác nhận lịch cũ không bị chặn vô cớ.
            if (doiGio || doiNV)
                kiemTraCaLamViec(l);
            kiemTraTrungLich(l, l.getMaLichHen());
        }
        return dao.capNhat(l, DO_DAI_LICH_PHUT);
    }

    /** Đổi nhanh trạng thái (Xác nhận / Hoàn thành / Hủy lịch) theo máy trạng thái. */
    public boolean doiTrangThai(String ma, TrangThaiLichHen moi) {
        if (moi == null)
            throw new IllegalArgumentException("Trạng thái mới không hợp lệ.");
        LichHen cu = timTheoMa(ma);
        kiemTraChoPhepSua(cu);
        TrangThaiLichHen ttCu = TrangThaiLichHen.tuTen(cu.getTrangThai());
        if (ttCu == moi)
            throw new IllegalArgumentException("Lịch hẹn đã ở trạng thái \"" + moi + "\".");
        if (!ttCu.coTheChuyenSang(moi))
            throw new IllegalArgumentException("Không thể chuyển lịch hẹn từ \"" + ttCu + "\" sang \"" + moi + "\".");
        if (moi == TrangThaiLichHen.HOAN_THANH)
            kiemTraDaDenGioHen(cu.getThoiGianHen());
        return dao.capNhatTrangThai(cu.getMaLichHen(), moi.getTenHienThi());
    }

    /**
     * Chỉ xóa lịch "Chờ xác nhận" (nhập nhầm) hoặc "Đã hủy". Lịch đã xác nhận phải hủy chứ không xóa, và lịch
     * đã tiếp nhận / hoàn thành được giữ làm lịch sử.
     */
    public boolean xoa(String ma) {
        ma = KiemTra.maBatBuoc(ma, "lịch hẹn");
        LichHen cu = KiemTra.phaiTonTai(dao.timTheoMa(ma), "lịch hẹn");
        TrangThaiLichHen tt = TrangThaiLichHen.tuTen(cu.getTrangThai());
        if (tt != TrangThaiLichHen.CHO_XAC_NHAN && tt != TrangThaiLichHen.DA_HUY) {
            throw new IllegalArgumentException("Chỉ xóa được lịch hẹn ở trạng thái \"Chờ xác nhận\" hoặc \"Đã hủy\". "
                    + "Lịch đang \"" + cu.getTrangThai() + "\" "
                    + (tt != null && tt.chuaKetThuc() && !tt.doTiepNhanQuanLy()
                            ? "hãy dùng chức năng Hủy lịch để giữ lịch sử."
                            : "được giữ lại làm lịch sử."));
        }
        return dao.xoa(ma);
    }

    // ---------------------------------------------------------------- các luật kiểm tra

    private void kiemTraDaDenGioHen(LocalDateTime thoiGianHen) {
        if (thoiGianHen.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Chưa đến giờ hẹn (" + thoiGianHen.format(NGAY_GIO)
                    + ") nên chưa thể đánh dấu Hoàn thành.");
    }

    /** Khách hàng, thú cưng, nhân viên phải tồn tại và khớp nhau. */
    private void kiemTraThamChieu(LichHen l) {
        String maKH = l.getMaKH().getMaKH().trim();
        String maTC = l.getMaThuCung().getMaThuCung().trim();
        String maNV = l.getMaNV().getMaNV().trim();

        if (khachHangDAO.timTheoMa(maKH) == null)
            throw new IllegalArgumentException("Khách hàng " + maKH + " không tồn tại hoặc đã bị xóa.");

        ThuCung tc = thuCungDAO.timTheoMa(maTC);
        if (tc == null)
            throw new IllegalArgumentException("Thú cưng " + maTC + " không tồn tại hoặc đã bị xóa.");
        if (tc.getMaKH() == null || !maKH.equalsIgnoreCase(tc.getMaKH().getMaKH()))
            throw new IllegalArgumentException("Thú cưng " + maTC + " không thuộc khách hàng " + maKH + ".");

        if (!dao.nhanVienNhanLichDuoc(maNV))
            throw new IllegalArgumentException("Nhân viên " + maNV
                    + " không tồn tại, đã ngừng hoạt động hoặc không thuộc bộ phận chăm sóc nên không nhận được lịch hẹn.");
    }

    /**
     * Cả khoảng [giờ hẹn, giờ hẹn + 30 phút] phải nằm trong ca làm việc của nhân viên ngày đó.
     * Các ca liền nhau (ví dụ ca sáng 07:00–12:00 và ca chiều 12:00–17:00) được gộp thành một khoảng.
     */
    private void kiemTraCaLamViec(LichHen l) {
        String maNV = l.getMaNV().getMaNV().trim();
        LocalDateTime batDau = l.getThoiGianHen();
        LocalDateTime ketThuc = batDau.plusMinutes(DO_DAI_LICH_PHUT);
        if (!ketThuc.toLocalDate().equals(batDau.toLocalDate()))
            throw new IllegalArgumentException("Lịch hẹn không được kéo dài sang ngày hôm sau.");

        LocalDate ngay = batDau.toLocalDate();
        List<LocalTime[]> caLam = dao.layCaLamTrongNgay(maNV, ngay);
        String tenNV = l.getMaNV().getTenNV() == null ? maNV : maNV + " - " + l.getMaNV().getTenNV();
        if (caLam.isEmpty())
            throw new IllegalArgumentException("Nhân viên " + tenNV + " không có ca làm việc ngày " + ngay.format(NGAY)
                    + ". Vui lòng xếp lịch làm việc cho nhân viên hoặc chọn nhân viên khác.");

        List<LocalTime[]> khoang = gopCa(caLam);
        LocalTime tu = batDau.toLocalTime(), den = ketThuc.toLocalTime();
        StringBuilder cacCa = new StringBuilder();
        for (LocalTime[] k : khoang) {
            if (!tu.isBefore(k[0]) && !den.isAfter(k[1]))
                return;
            if (cacCa.length() > 0)
                cacCa.append(", ");
            cacCa.append(k[0].format(GIO)).append("–").append(k[1].format(GIO));
        }
        throw new IllegalArgumentException("Khung giờ " + tu.format(GIO) + "–" + den.format(GIO)
                + " nằm ngoài ca làm việc của nhân viên " + tenNV + " ngày " + ngay.format(NGAY)
                + " (ca làm: " + cacCa + ").");
    }

    /** Nhân viên và thú cưng đều không được có lịch khác chồng lên khoảng [giờ hẹn, giờ hẹn + 30 phút). */
    private void kiemTraTrungLich(LichHen l, String maBoQua) {
        LocalDateTime batDau = l.getThoiGianHen();
        List<LichHen> trungNV = dao.timLichChong("maNV", l.getMaNV().getMaNV().trim(), batDau, DO_DAI_LICH_PHUT,
                maBoQua);
        if (!trungNV.isEmpty()) {
            LichHen cu = trungNV.get(0);
            throw new IllegalArgumentException("Nhân viên " + l.getMaNV().getMaNV() + " đã có lịch " + cu.getMaLichHen()
                    + " lúc " + cu.getThoiGianHen().format(NGAY_GIO) + ". Mỗi lịch kéo dài " + DO_DAI_LICH_PHUT
                    + " phút nên không thể đặt chồng lấn.");
        }
        List<LichHen> trungTC = dao.timLichChong("maThuCung", l.getMaThuCung().getMaThuCung().trim(), batDau,
                DO_DAI_LICH_PHUT, maBoQua);
        if (!trungTC.isEmpty()) {
            LichHen cu = trungTC.get(0);
            throw new IllegalArgumentException("Thú cưng " + l.getMaThuCung().getMaThuCung() + " đã có lịch "
                    + cu.getMaLichHen() + " lúc " + cu.getThoiGianHen().format(NGAY_GIO)
                    + ", không thể ở hai lịch cùng lúc.");
        }
    }

    /** Kiểm tra các trường bắt buộc và chuẩn hóa dữ liệu. Mã lịch hẹn chỉ bắt buộc khi sửa. */
    private void kiemTraDuLieuCoBan(LichHen l) {
        if (l == null)
            throw new IllegalArgumentException("Thông tin lịch hẹn không hợp lệ.");
        boolean coMa = l.getMaLichHen() != null && !l.getMaLichHen().trim().isEmpty();
        if (coMa && l.getMaLichHen().trim().length() > 20)
            throw new IllegalArgumentException("Mã lịch hẹn tối đa 20 ký tự.");
        if (l.getThoiGianHen() == null)
            throw new IllegalArgumentException("Vui lòng nhập thời gian hẹn.");
        if (TrangThaiLichHen.tuTen(l.getTrangThai()) == null)
            throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ.");
        if (l.getMaNV() == null || l.getMaNV().getMaNV() == null || l.getMaNV().getMaNV().trim().isEmpty())
            throw new IllegalArgumentException("Vui lòng chọn nhân viên.");
        if (l.getMaKH() == null || l.getMaKH().getMaKH() == null || l.getMaKH().getMaKH().trim().isEmpty())
            throw new IllegalArgumentException("Vui lòng chọn khách hàng.");
        if (l.getMaThuCung() == null || l.getMaThuCung().getMaThuCung() == null
                || l.getMaThuCung().getMaThuCung().trim().isEmpty())
            throw new IllegalArgumentException("Vui lòng chọn thú cưng.");

        if (coMa)
            l.setMaLichHen(l.getMaLichHen().trim());
        l.setTrangThai(TrangThaiLichHen.tuTen(l.getTrangThai()).getTenHienThi());
        l.getMaNV().setMaNV(l.getMaNV().getMaNV().trim());
        l.getMaKH().setMaKH(l.getMaKH().getMaKH().trim());
        l.getMaThuCung().setMaThuCung(l.getMaThuCung().getMaThuCung().trim());
    }
}

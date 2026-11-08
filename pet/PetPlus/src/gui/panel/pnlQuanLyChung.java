package gui.panel;

import utils.PetPlusTheme;
import utils.ThongBao;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Khung chung của các màn hình quản lý (khách hàng, thú cưng, lịch hẹn, dịch vụ).
 * Mọi phần giống nhau nằm ở đây để bốn màn hình luôn đồng bộ: tiêu đề, thanh tìm kiếm (Enter, nút Tìm kiếm,
 * tự tìm khi gõ), bảng cùng một kiểu và một bộ màu, hàng nút Thêm / Sửa / Xóa, cách chọn dòng, cách xác nhận
 * xóa và cách báo lỗi. Lớp con chỉ khai báo phần riêng: cột, dữ liệu, dialog và bộ lọc phụ.
 * Lớp con gọi {@link #khoiTao()} ở cuối constructor.
 */
public abstract class pnlQuanLyChung<T> extends JPanel {
    private static final Color MAU_TIEU_DE = new Color(224, 25, 91);
    private static final Color MAU_CHU = new Color(32, 44, 64);
    private static final Color MAU_VIEN = new Color(237, 217, 225);
    private static final Color MAU_NEN_DAU_BANG = new Color(255, 236, 243);
    private static final Color MAU_LOI = new Color(200, 30, 60);

    protected final JTextField txtTimKiem = new JTextField(22);
    protected final JButton btnTimKiem = new JButton("Tìm kiếm");
    protected final JButton btnLamMoi = new JButton("Làm mới");
    protected final JButton btnThem = new JButton("Thêm");
    protected final JButton btnSua = new JButton("Sửa");
    protected final JButton btnXoa = new JButton("Xóa");
    /** Hàng tìm kiếm; lớp con thêm bộ lọc riêng vào đây. */
    protected final JPanel pnlBoLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    /** Hàng nút phía dưới, căn phải; lớp con có thể chèn thêm nút thao tác riêng phía trước Thêm / Sửa / Xóa. */
    protected final JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
    protected final DefaultTableModel model;
    protected final JTable table;

    private final JLabel lblTieuDe;
    private final JLabel lblKetQua = new JLabel(" ");
    private final JScrollPane scrollBang;
    private final String tenDoiTuong;
    private final Timer boDemTimKiem;

    protected pnlQuanLyChung(String tieuDe, String tenDoiTuong, String goiYTimKiem, String[] cot) {
        this.tenDoiTuong = tenDoiTuong;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 22));
        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setBackground(Color.WHITE);
        pnlTieuDe.add(lblTieuDe, BorderLayout.WEST);

        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTimKiem.setToolTipText(goiYTimKiem);
        pnlBoLoc.setBackground(Color.WHITE);
        pnlBoLoc.add(new JLabel("Tìm kiếm:"));
        pnlBoLoc.add(txtTimKiem);
        pnlBoLoc.add(btnTimKiem);
        pnlBoLoc.add(btnLamMoi);

        lblKetQua.setBorder(BorderFactory.createEmptyBorder(0, 12, 4, 0));
        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setBackground(Color.WHITE);
        pnlTren.add(pnlTieuDe, BorderLayout.NORTH);
        pnlTren.add(pnlBoLoc, BorderLayout.CENTER);
        pnlTren.add(lblKetQua, BorderLayout.SOUTH);
        add(pnlTren, BorderLayout.NORTH);

        model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        scrollBang = new JScrollPane(table);
        add(scrollBang, BorderLayout.CENTER);

        pnlNut.setBackground(Color.WHITE);
        pnlNut.add(btnThem);
        pnlNut.add(btnSua);
        pnlNut.add(btnXoa);
        add(pnlNut, BorderLayout.SOUTH);

        // Gõ tới đâu tìm tới đó, nhưng chờ 300 ms sau phím cuối để không truy vấn CSDL liên tục.
        boDemTimKiem = new Timer(300, e -> taiLai());
        boDemTimKiem.setRepeats(false);
    }

    // ------------------------------------------------------------ phần lớp con khai báo

    /** Dữ liệu cần hiển thị theo từ khóa (rỗng = tất cả) và các bộ lọc riêng của màn hình. */
    protected abstract List<T> layDuLieu(String tuKhoa);

    /** Một dòng của bảng; cột 0 luôn là mã dùng để chọn dòng. */
    protected abstract Object[] taoDong(T doiTuong);

    protected abstract T timTheoMa(String ma);

    /** Mở dialog thêm (cu = null) hoặc sửa; trả về true nếu đã lưu thành công. */
    protected abstract boolean moDialog(T cu);

    protected abstract boolean xoaTheoMa(String ma);

    /** Ai được thêm / sửa / xóa. */
    protected abstract boolean coQuyenCapNhat();

    /** Dòng cảnh báo bổ sung trong hộp xác nhận xóa. */
    protected String canhBaoKhiXoa() {
        return "";
    }

    /** Ném IllegalArgumentException nếu đối tượng không được phép sửa. */
    protected void kiemTraTruocKhiSua(T doiTuong) {
    }

    /** Đưa bộ lọc riêng về mặc định khi bấm Làm mới. */
    protected void datLaiBoLoc() {
    }

    // ------------------------------------------------------------ xử lý chung

    protected final void khoiTao() {
        boolean quyen = coQuyenCapNhat();
        btnThem.setVisible(quyen);
        btnSua.setVisible(quyen);
        btnXoa.setVisible(quyen);

        btnTimKiem.addActionListener(e -> taiNgay());
        txtTimKiem.addActionListener(e -> taiNgay());
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                boDemTimKiem.restart();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                boDemTimKiem.restart();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                boDemTimKiem.restart();
            }
        });
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            datLaiBoLoc();
            taiNgay();
        });
        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());

        PetPlusTheme.apply(this);
        dinhDangGiaoDien(); // sau khi áp theme để màu bảng luôn giống nhau ở cả 4 màn hình
        taiLai();
    }

    private void taiNgay() {
        boDemTimKiem.stop();
        taiLai();
    }

    /** Nạp lại bảng theo từ khóa và bộ lọc hiện tại. */
    public final void taiLai() {
        String tuKhoa = txtTimKiem.getText().trim();
        try {
            List<T> ds = layDuLieu(tuKhoa);
            model.setRowCount(0);
            for (T t : ds)
                model.addRow(taoDong(t));
            lblKetQua.setText(ds.isEmpty() && !tuKhoa.isEmpty()
                    ? "Không tìm thấy " + tenDoiTuong + " nào khớp với \"" + tuKhoa + "\"."
                    : " ");
        } catch (RuntimeException ex) {
            ThongBao.xuLy(this, "Không thể tải danh sách " + tenDoiTuong, ex);
        }
    }

    protected final String maDongChon() {
        int dong = table.getSelectedRow();
        return dong < 0 ? null : String.valueOf(model.getValueAt(table.convertRowIndexToModel(dong), 0));
    }

    /**
     * Đọc đối tượng của dòng đang chọn từ CSDL (dữ liệu mới nhất, không dùng dữ liệu cũ trên bảng).
     * Trả về null và đã báo người dùng nếu chưa chọn dòng hoặc đối tượng không còn.
     */
    protected final T docDongChon(String hanhDong) {
        String ma = maDongChon();
        if (ma == null) {
            ThongBao.info(this, "Vui lòng chọn " + tenDoiTuong + " cần " + hanhDong + ".");
            return null;
        }
        try {
            T doiTuong = timTheoMa(ma);
            if (doiTuong == null) {
                ThongBao.info(this, "Không tìm thấy " + tenDoiTuong + " " + ma + ". Có thể đã bị người khác xóa.");
                taiLai();
            }
            return doiTuong;
        } catch (RuntimeException ex) {
            ThongBao.xuLy(this, "Không thể đọc " + tenDoiTuong, ex);
            return null;
        }
    }

    protected final Window cuaSoCha() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void them() {
        if (moDialog(null))
            taiLai();
    }

    private void sua() {
        T doiTuong = docDongChon("sửa");
        if (doiTuong == null)
            return;
        try {
            kiemTraTruocKhiSua(doiTuong);
        } catch (IllegalArgumentException ex) {
            ThongBao.canhBao(this, "Không thể sửa " + tenDoiTuong, ex.getMessage());
            return;
        }
        if (moDialog(doiTuong))
            taiLai();
    }

    private void xoa() {
        String ma = maDongChon();
        if (ma == null) {
            ThongBao.info(this, "Vui lòng chọn " + tenDoiTuong + " cần xóa.");
            return;
        }
        String canhBao = canhBaoKhiXoa();
        if (!ThongBao.xacNhan(this, "Bạn có chắc muốn xóa " + tenDoiTuong + " " + ma + "?"
                + (canhBao.isEmpty() ? "" : "\n" + canhBao)))
            return;
        try {
            if (xoaTheoMa(ma)) {
                ThongBao.info(this, "Đã xóa " + tenDoiTuong + ".");
                taiLai();
            } else {
                ThongBao.info(this, "Không xóa được " + tenDoiTuong + ".");
            }
        } catch (RuntimeException ex) {
            ThongBao.xuLy(this, "Không thể xóa " + tenDoiTuong, ex);
        }
    }

    /** Một bộ định dạng duy nhất cho bảng của cả 4 màn hình. */
    private void dinhDangGiaoDien() {
        lblTieuDe.setForeground(MAU_TIEU_DE);
        lblKetQua.setForeground(MAU_LOI);
        lblKetQua.setFont(new Font("Arial", Font.ITALIC, 13));

        Color chu = PetPlusTheme.paintColor(MAU_CHU);
        Color vien = PetPlusTheme.paintColor(MAU_VIEN);
        Color nenDau = PetPlusTheme.paintColor(MAU_NEN_DAU_BANG);
        Color trang = PetPlusTheme.paintColor(Color.WHITE);

        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(chu);
        table.setBackground(trang);
        table.setRowHeight(36);
        table.setGridColor(vien);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(nenDau);
        table.setSelectionForeground(chu);

        JTableHeader dau = table.getTableHeader();
        dau.setFont(new Font("Arial", Font.BOLD, 13));
        dau.setForeground(chu);
        dau.setBackground(nenDau);
        dau.setPreferredSize(new Dimension(0, 42));
        dau.setReorderingAllowed(false);

        DefaultTableCellRenderer o = new DefaultTableCellRenderer();
        o.setBackground(trang);
        o.setForeground(chu);
        o.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, vien));
        o.setVerticalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(o);

        scrollBang.setBorder(BorderFactory.createLineBorder(vien));
        scrollBang.getViewport().setBackground(trang);
    }
}

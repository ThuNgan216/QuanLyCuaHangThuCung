package gui.panel;

import dao.HoaDonDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import utils.PetPlusTheme;

/** Báo cáo tổng doanh thu và số hóa đơn theo ngày. */
public class pnlBaoBieuDoanhThu extends JPanel {
    private static final Color PRIMARY = new Color(250, 48, 115);
    private static final Color MUTED = new Color(89, 111, 140);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final JSpinner spnTuNgay = taoSpinnerNgay(LocalDate.now().withDayOfMonth(1));
    private final JSpinner spnDenNgay = taoSpinnerNgay(LocalDate.now());
    private final JLabel lblTongDoanhThu = new JLabel("0 đ");
    private final JLabel lblTongHoaDon = new JLabel("0");
    private final JLabel lblSoNgay = new JLabel("Chưa có dữ liệu");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Ngày", "Số hóa đơn", "Doanh thu" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public pnlBaoBieuDoanhThu() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        taoGiaoDien();
        taiDuLieuBaoCao();
        PetPlusTheme.apply(this);
    }

    private void taoGiaoDien() {
        JPanel pnlTren = new JPanel(new BorderLayout(0, 12));
        pnlTren.setOpaque(false);

        JLabel lblTieuDe = new JLabel("BÁO BIỂU DOANH THU");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 22));
        lblTieuDe.setForeground(new Color(224, 25, 91));
        pnlTren.add(lblTieuDe, BorderLayout.NORTH);

        JPanel pnlBoLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlBoLoc.setOpaque(false);
        pnlBoLoc.add(new JLabel("Từ ngày:"));
        pnlBoLoc.add(spnTuNgay);
        pnlBoLoc.add(new JLabel("Đến ngày:"));
        pnlBoLoc.add(spnDenNgay);

        JButton btnXem = new JButton("Xuất báo cáo");
        btnXem.setBackground(PRIMARY);
        btnXem.setForeground(Color.WHITE);
        btnXem.addActionListener(e -> {
            if (taiDuLieuBaoCao()) {
                xuatBaoCaoCsv();
            }
        });
        pnlBoLoc.add(btnXem);
        pnlTren.add(pnlBoLoc, BorderLayout.CENTER);

        JPanel pnlThongKe = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlThongKe.setOpaque(false);
        pnlThongKe.add(taoTheThongKe("Tổng doanh thu", lblTongDoanhThu, new Color(253, 236, 241)));
        pnlThongKe.add(taoTheThongKe("Tổng số hóa đơn", lblTongHoaDon, new Color(240, 244, 248)));
        pnlTren.add(pnlThongKe, BorderLayout.SOUTH);
        add(pnlTren, BorderLayout.NORTH);

        JTable tblDoanhThu = new JTable(model);
        tblDoanhThu.setRowHeight(30);
        tblDoanhThu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDoanhThu.setFillsViewportHeight(true);
        DefaultTableCellRenderer canPhai = new DefaultTableCellRenderer();
        canPhai.setHorizontalAlignment(SwingConstants.RIGHT);
        tblDoanhThu.getColumnModel().getColumn(1).setCellRenderer(canPhai);
        tblDoanhThu.getColumnModel().getColumn(2).setCellRenderer(canPhai);
        add(new JScrollPane(tblDoanhThu), BorderLayout.CENTER);

        JPanel pnlChan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlChan.setOpaque(false);
        lblSoNgay.setForeground(MUTED);
        pnlChan.add(lblSoNgay);
        add(pnlChan, BorderLayout.SOUTH);
    }

    private JPanel taoTheThongKe(String tieuDe, JLabel lblGiaTri, Color mauNen) {
        JPanel the = new JPanel(new BorderLayout(5, 5));
        the.setBackground(mauNen);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 225, 233)),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(new Font("Arial", Font.PLAIN, 13));
        lblTieuDe.setForeground(MUTED);
        lblGiaTri.setFont(new Font("Arial", Font.BOLD, 20));
        lblGiaTri.setForeground(new Color(33, 37, 41));

        the.add(lblTieuDe, BorderLayout.NORTH);
        the.add(lblGiaTri, BorderLayout.CENTER);
        return the;
    }

    private boolean taiDuLieuBaoCao() {
        try {
            LocalDate tuNgay = layNgay(spnTuNgay);
            LocalDate denNgay = layNgay(spnDenNgay);
            if (tuNgay.isAfter(denNgay)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.",
                        "Khoảng thời gian không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            List<Object[]> duLieu = hoaDonDAO.layBaoCaoDoanhThu(tuNgay, denNgay);
            model.setRowCount(0);
            int tongHoaDon = 0;
            java.math.BigDecimal tongDoanhThu = java.math.BigDecimal.ZERO;

            for (Object[] dong : duLieu) {
                LocalDate ngay = (LocalDate) dong[0];
                int soHoaDon = ((Number) dong[1]).intValue();
                java.math.BigDecimal doanhThu = (java.math.BigDecimal) dong[2];
                if (doanhThu == null) {
                    doanhThu = java.math.BigDecimal.ZERO;
                }

                tongHoaDon += soHoaDon;
                tongDoanhThu = tongDoanhThu.add(doanhThu);
                model.addRow(new Object[] {
                        ngay.format(DATE_FORMAT),
                        String.format("%,d", soHoaDon),
                        String.format("%,.0f đ", doanhThu.doubleValue())
                });
            }

            lblTongHoaDon.setText(String.format("%,d", tongHoaDon));
            lblTongDoanhThu.setText(String.format("%,.0f đ", tongDoanhThu.doubleValue()));
            lblSoNgay.setText("Hiển thị " + duLieu.size() + " ngày có phát sinh hóa đơn trong "
                    + tuNgay.format(DATE_FORMAT) + " – " + denNgay.format(DATE_FORMAT));
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải báo biểu doanh thu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void xuatBaoCaoCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo biểu doanh thu");
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp CSV (*.csv)", "csv"));
        chooser.setSelectedFile(new File("BaoCaoDoanhThu_" + LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File tep = chooser.getSelectedFile();
        if (!tep.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            tep = new File(tep.getParentFile(), tep.getName() + ".csv");
        }
        if (tep.exists()) {
            int luaChon = JOptionPane.showConfirmDialog(this,
                    "Tệp đã tồn tại. Bạn có muốn ghi đè không?", "Xác nhận ghi đè",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (luaChon != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(tep.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            ghiDongCsv(writer, "Báo biểu doanh thu");
            ghiDongCsv(writer, "Từ ngày", layNgay(spnTuNgay).format(DATE_FORMAT));
            ghiDongCsv(writer, "Đến ngày", layNgay(spnDenNgay).format(DATE_FORMAT));
            ghiDongCsv(writer, "Tổng doanh thu", lblTongDoanhThu.getText());
            ghiDongCsv(writer, "Tổng số hóa đơn", lblTongHoaDon.getText());
            writer.newLine();
            ghiDongCsv(writer, "Ngày", "Số hóa đơn", "Doanh thu");
            for (int row = 0; row < model.getRowCount(); row++) {
                ghiDongCsv(writer,
                        model.getValueAt(row, 0),
                        model.getValueAt(row, 1),
                        model.getValueAt(row, 2));
            }
            JOptionPane.showMessageDialog(this, "Đã xuất báo biểu đến:\n" + tep.getAbsolutePath(),
                    "Xuất báo cáo thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu tệp báo cáo: " + ex.getMessage(),
                    "Lỗi xuất báo cáo", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ghiDongCsv(BufferedWriter writer, Object... giaTri) throws IOException {
        for (int i = 0; i < giaTri.length; i++) {
            if (i > 0) {
                writer.write(';');
            }
            String chuoi = String.valueOf(giaTri[i]).replace("\"", "\"\"");
            writer.write('"');
            writer.write(chuoi);
            writer.write('"');
        }
        writer.newLine();
    }

    private LocalDate layNgay(JSpinner spinner) {
        Date ngay = (Date) spinner.getValue();
        return ngay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static JSpinner taoSpinnerNgay(LocalDate ngay) {
        Date giaTri = Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner spinner = new JSpinner(new SpinnerDateModel(giaTri, null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }
}

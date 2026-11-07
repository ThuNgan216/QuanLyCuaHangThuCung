
package gui.panel;

import dao.NhanVienDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import gui.dialogs.dlgNhanVien.PetPlusUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class pnlTimKiemNhanVien extends JPanel {

    private final NhanVienDAO dao = new NhanVienDAO();

    private final JTextField txtTim = PetPlusUI.textField();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {
                    "Mã NV", "Họ tên", "Số điện thoại", "CCCD",
                    "Địa chỉ", "Email", "Chức vụ", "Trạng thái"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tblNV = new JTable(model);

    private final JButton btnTim =
            PetPlusUI.button("Tìm kiếm", true);

    private final JButton btnLamMoi =
            PetPlusUI.button("Làm mới", false);


    public pnlTimKiemNhanVien() {
        initComponents();
        initEvents();
        timKiem();
    }


    private void initComponents() {

        setLayout(new BorderLayout(0, 20));
        setBackground(PetPlusUI.BACKGROUND);

        setBorder(BorderFactory.createEmptyBorder(
                25, 30, 25, 30
        ));

        JLabel lblTitle = new JLabel("TÌM KIẾM NHÂN VIÊN");
        lblTitle.setFont(PetPlusUI.font(Font.BOLD, 25));
        lblTitle.setForeground(PetPlusUI.PRIMARY);

        JPanel pnlTitle = new JPanel(new GridLayout(1, 1));
        pnlTitle.setOpaque(false);
        pnlTitle.add(lblTitle);

        JLabel lblTim = PetPlusUI.label("Tìm nhân viên:");

        txtTim.setToolTipText(
                "Nhập mã nhân viên, họ tên hoặc số điện thoại"
        );

        btnTim.setPreferredSize(new Dimension(160, 42));
        btnLamMoi.setPreferredSize(new Dimension(160, 42));

        JPanel pnlButtons = new JPanel(
                new GridLayout(1, 2, 12, 0)
        );
        pnlButtons.setOpaque(false);

        pnlButtons.add(btnTim);
        pnlButtons.add(btnLamMoi);

        JPanel pnlSearch = new JPanel(
                new BorderLayout(15, 0)
        );
        pnlSearch.setBackground(PetPlusUI.WHITE);
        pnlSearch.setBorder(BorderFactory.createEmptyBorder(
                18, 20, 18, 20
        ));

        pnlSearch.add(lblTim, BorderLayout.WEST);
        pnlSearch.add(txtTim, BorderLayout.CENTER);
        pnlSearch.add(pnlButtons, BorderLayout.EAST);

        // Panel trên cùng
        JPanel pnlTop = new JPanel(
                new BorderLayout(0, 25)
        );
        pnlTop.setOpaque(false);

        pnlTop.add(pnlTitle, BorderLayout.NORTH);
        pnlTop.add(pnlSearch, BorderLayout.CENTER);

        add(pnlTop, BorderLayout.NORTH);

        // 2. BẢNG DANH SÁCH NHÂN VIÊN
        PetPlusUI.styleTable(tblNV);

        tblNV.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblNV.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF
        );

        tblNV.getColumnModel().getColumn(0).setPreferredWidth(90);
        tblNV.getColumnModel().getColumn(1).setPreferredWidth(170);
        tblNV.getColumnModel().getColumn(2).setPreferredWidth(130);
        tblNV.getColumnModel().getColumn(3).setPreferredWidth(140);
        tblNV.getColumnModel().getColumn(4).setPreferredWidth(190);
        tblNV.getColumnModel().getColumn(5).setPreferredWidth(210);
        tblNV.getColumnModel().getColumn(6).setPreferredWidth(170);
        tblNV.getColumnModel().getColumn(7).setPreferredWidth(130);

        JScrollPane scrollPane =
                PetPlusUI.scrollTable(tblNV);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void initEvents() {

        btnTim.addActionListener(e -> timKiem());

        txtTim.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> {
            txtTim.setText("");
            timKiem();
            txtTim.requestFocusInWindow();
        });
    }
    private void timKiem() {

        String tuKhoa = txtTim.getText().trim();

        try {

            List<NhanVien> danhSach = dao.timKiem(tuKhoa);

        
            model.setRowCount(0);

            for (NhanVien nv : danhSach) {

                TaiKhoan tk = nv.getMaTK();

                model.addRow(new Object[] {
                        nv.getMaNV(),
                        nv.getTenNV(),
                        nv.getSdt(),
                        nv.getCccd(),
                        nv.getDiaChi(),
                        nv.getEmail(),
                        nv.getChucVu(),
                        tk == null ? "" : tk.getTrangThai()
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tìm kiếm nhân viên:\n"
                            + e.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    public void taiLaiDuLieu() {
        txtTim.setText("");
        timKiem();
    }
}

package gui.main;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import gui.panel.pnlHoaDon;
import gui.panel.pnlThuCung;
import gui.panel.pnlTichDiem;
import gui.panel.pnlXuatHoaDon;
import gui.panel.pnlDichVu;
import gui.panel.pnlBaoBieuNhapHang;
import gui.panel.pnlBaoBieuDoanhThu;
import gui.panel.pnlBaoBieuLoiNhuan;
import gui.panel.pnlKhuyenMai;
import gui.panel.pnlNhaCungCap;
import gui.panel.pnlNhapHang;
import gui.panel.pnlTimKiemPhieuNhap;
import gui.panel.pnlTonKho;
import gui.panel.pnlTiepNhanThuCung;
import gui.panel.pnlLichHen;
import gui.panel.pnlKhachHang;
import gui.panel.pnlQuanLyHoaDon;
import gui.panel.pnlNhanVien;
import utils.Auth;
import dao.HoaDonDAO;
import dao.SanPhamDAO;
import gui.dialogs.dlgDoiMatKhau;
import gui.panel.pnlSanPham;
import gui.panel.pnlThanhToan;
import gui.panel.pnlXemLichLamViec;
import gui.panel.pnlXepLichLamViec;

public class MainFrame extends JFrame {
    private static final Color PRIMARY = new Color(250, 48, 115);
    private static final Color PRIMARY_DARK = new Color(224, 25, 91);
    private static final Color TEXT = new Color(31, 45, 70);
    private static final Color MUTED = new Color(89, 111, 140);
    private static final Color BG = new Color(252, 250, 251);
    private static final Color BORDER = new Color(240, 225, 231);
    private static final Color WHITE = Color.WHITE;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JPanel homePage;
    private JLabel pageTitle;
    private JButton btnLamMoiTrangChu;
    private JPanel topProductsList;
    private DefaultTableModel recentOrdersModel;
    private RevenueChart revenueChart;
    private pnlNhanVien employeePanel;
    private pnlThanhToan paymentPanel;
    private JLabel lblDate;
    // Các chỉ số thống kê trên Dashboard
    private JLabel lblDonHangHomNay;
    private JLabel lblDichVuHomNay;
    private JLabel lblSanPhamHomNay;
    private JLabel lblDoanhThuHomNay;
    private pnlXemLichLamViec xemLichPanel;
    private pnlXepLichLamViec xepLichPanel;

    // Thanh menu ngang
    private final String[] MENU_ITEMS = { "1. Hệ thống", "2. Danh mục", "3. Cập nhật", "4. Xử lý", "5. Tìm kiếm",
            "6. Thống kê" };
    private final String[] MENU_ICONS = { "settings.png", "box.png", "employee.png", "cart.png", "search.png",
            "chart.png" };
    // Menu con theo từng nhóm chức năng (thả xuống khi bấm vào nhóm)
    private final String[][] SUB_MENUS = {
            { "Đổi mật khẩu tài khoản", "Phân quyền vai trò", "Đăng xuất tài khoản", "Thoát ứng dụng" },
            { "Nhân viên", "Khách hàng", "Nhà cung cấp", "Nhập hàng", "Lịch làm việc" },
            { "Thú cưng", "Lịch hẹn", "Sản phẩm", "Dịch vụ", "Khuyến mãi" },
            { "Tiếp nhận thú cưng", "Lập hóa đơn", "Xếp lịch làm việc", "Xuất hóa đơn",
                    "Cảnh báo tồn kho" },
            { "Sản phẩm", "Khách hàng", "Tích điểm", "Hóa đơn", "Lịch hẹn", "Nhân viên", "Dịch vụ", "Phiếu nhập" },
            { "Doanh thu", "Lợi nhuận", "Nhập hàng" }
    };

    // Phím tắt hiển thị bên phải mục menu
    private static final java.util.Map<String, KeyStroke> SHORTCUTS = new HashMap<>();
    static {
        SHORTCUTS.put("Đổi mật khẩu tài khoản", KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        SHORTCUTS.put("Phân quyền vai trò", KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        SHORTCUTS.put("Bàn giao ca / Chốt két", KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        SHORTCUTS.put("Đăng xuất tài khoản", KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        SHORTCUTS.put("Thoát ứng dụng", KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
    }

    private final java.util.Set<String> registeredCards = new HashSet<>();

    public MainFrame() {
        setTitle("HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG");
        setSize(1100, 700); // Kích thước form chính
        setMinimumSize(new Dimension(1000, 650)); // Kích thước tối thiểu
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        // 1. VÙNG NỘI DUNG CHÍNH
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG);
        homePage = createHomePage();
        registerCard("Trang chủ", homePage);

        // 2. THANH MENU NGANG
        setJMenuBar(createMenuBar());

        // 3. THANH TIÊU ĐỀ PHÍA TRÊN
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setBackground(BG);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHITE);
        headerPanel.setBorder(new EmptyBorder(12, 18, 7, 22));
        pageTitle = label("Trang chủ", 19, TEXT, true);
        // Icon lịch ngày giờ
        lblDate = label("", 13, TEXT, false);
        ImageIcon calendarIcon = loadIcon("Calendar.png", 24, 24);
        if (calendarIcon != null) {
            lblDate.setIcon(calendarIcon);
            lblDate.setIconTextGap(10);
        }
        btnLamMoiTrangChu = new JButton("Làm mới");
        btnLamMoiTrangChu.setFont(new Font("Times New Roman", Font.BOLD, 13));
        btnLamMoiTrangChu.setForeground(PRIMARY);
        btnLamMoiTrangChu.setBackground(WHITE);
        btnLamMoiTrangChu.setFocusPainted(false);
        btnLamMoiTrangChu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLamMoiTrangChu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY), new EmptyBorder(7, 14, 7, 14)));
        btnLamMoiTrangChu.setToolTipText("Tải lại số liệu và danh sách đơn hàng trên trang chủ");
        btnLamMoiTrangChu.addActionListener(e -> refreshDashboard());
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerActions.setOpaque(false);
        headerActions.add(btnLamMoiTrangChu);
        headerActions.add(lblDate);
        headerPanel.add(pageTitle, BorderLayout.WEST);
        headerPanel.add(headerActions, BorderLayout.EAST);
        rightSide.add(headerPanel, BorderLayout.NORTH);
        rightSide.add(cardPanel, BorderLayout.CENTER);
        add(rightSide, BorderLayout.CENTER);

        // 4. KHỞI TẠO CÁC PANEL CÓ SẴN
        employeePanel = new pnlNhanVien();
        registerCard("Nhân viên", employeePanel);

        // Khởi tạo giao diện tạo đơn hàng
        // Giao diện bán hàng và dịch vụ
        paymentPanel = new pnlThanhToan();
        registerCard("Lập hóa đơn", paymentPanel);

        // Đăng ký trang quản lý hóa đơn
        registerCard("Quản lý hóa đơn", new pnlHoaDon());
        registerCard("Hóa đơn", createExistingPanel("Hóa đơn"));
        registerCard("Khách hàng", new pnlKhachHang());
        registerCard("Thú cưng", new pnlThuCung());
        registerCard("Tích điểm", new pnlTichDiem());
        registerCard("Dịch vụ", new pnlDichVu());
        registerCard("Lịch hẹn", new pnlLichHen());
        registerCard("Xuất hóa đơn", new pnlXuatHoaDon());
        xemLichPanel = new pnlXemLichLamViec();
        xepLichPanel = new pnlXepLichLamViec();

        xepLichPanel.setOnChanged(
                () -> xemLichPanel.chuyenDenTuan(xepLichPanel.getDauTuan()));

        registerCard("Lịch làm việc", xemLichPanel);
        registerCard("Xếp lịch làm việc", xepLichPanel);

        // 5. HIỂN THỊ THÔNG TIN TÀI KHOẢN (trên tiêu đề cửa sổ)
        updateAccountInfo();
        // Hiển thị giao diện sản phẩm trong vùng nội dung chính
        registerCard("Sản phẩm", new pnlSanPham());

        // Mở thẳng vào Dashboard
        showAction("Trang chủ", -1);
        refreshDashboard();

        javax.swing.Timer clock = new javax.swing.Timer(60000, e -> updateDate());
        clock.start();
        updateDate();

    }

    // Hiển thị thông tin người đăng nhập trên tiêu đề cửa sổ
    private void updateAccountInfo() {
        String ten = Auth.user != null ? Auth.user.getTenNV() : "Dev Test";
        String vaiTro = Auth.user != null ? Auth.user.getMaTK().getVaiTro() : "Chưa đăng nhập";
        setTitle("HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG - " + ten + " [" + vaiTro + "]");
    }

    // Tạo thanh menu ngang
    private JMenuBar createMenuBar() {
        // Màu khi rê chuột / mở menu (dùng màu chủ đạo của app)
        UIManager.put("Menu.selectionBackground", PRIMARY);
        UIManager.put("Menu.selectionForeground", WHITE);
        UIManager.put("MenuItem.selectionBackground", new Color(255, 226, 237));
        UIManager.put("MenuItem.selectionForeground", PRIMARY_DARK);

        JMenuBar bar = new JMenuBar();
        bar.setBackground(WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // Mục "0. Trang chủ": bấm vào là về Dashboard luôn, không thả xuống
        JMenu homeMenu = new JMenu("0. Trang chủ");
        homeMenu.setFont(new Font("Times New Roman", Font.BOLD, 14));
        homeMenu.setForeground(TEXT);
        homeMenu.setBorder(new EmptyBorder(8, 12, 8, 12));
        homeMenu.setIconTextGap(8);
        ImageIcon homeIcon = loadIcon("home.png", 18, 18);
        if (homeIcon != null)
            homeMenu.setIcon(homeIcon);
        homeMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                showAction("Trang chủ", -1);
                SwingUtilities.invokeLater(
                        () -> MenuSelectionManager.defaultManager().clearSelectedPath());
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        bar.add(homeMenu);

        for (int i = 0; i < MENU_ITEMS.length; i++) {
            JMenu menu = new JMenu(MENU_ITEMS[i]);
            menu.setFont(new Font("Times New Roman", Font.BOLD, 14));
            menu.setForeground(TEXT);
            menu.setBorder(new EmptyBorder(8, 12, 8, 12));
            menu.setIconTextGap(8);
            ImageIcon icon = loadIcon(MENU_ICONS[i], 18, 18);
            if (icon != null)
                menu.setIcon(icon);

            boolean hasVisibleItems = false;
            for (String item : SUB_MENUS[i]) {
                if (!coQuyen(item, i))
                    continue;
                // Đường kẻ ngăn cách trước nhóm đăng xuất / thoát
                if ("Đăng xuất tài khoản".equals(item))
                    menu.addSeparator();
                menu.add(createMenuItem(item, i));
                hasVisibleItems = true;
            }
            if (hasVisibleItems)
                bar.add(menu);
        }
        return bar;
    }

    /** Quyền hiển thị và mở chức năng được đối chiếu theo tác nhân trong SRS. */
    private boolean coQuyen(String action, int menuIndex) {
        if ("Trang chủ".equals(action)
                || "Đổi mật khẩu tài khoản".equals(action)
                || "Đăng xuất tài khoản".equals(action)
                || "Thoát ứng dụng".equals(action))
            return true;

        if (Auth.hasFullAccess())
            return true;

        String vaiTro = Auth.currentRole();
        if (vaiTro.isEmpty())
            return false;

        // Mục “Nhập hàng” trong nhóm Thống kê là báo cáo, dành cho quản lý.
        if ("Nhập hàng".equals(action) && menuIndex == 5)
            return false;

        switch (menuIndex) {
            case 0: // Hệ thống
                return false; // Phân quyền vai trò chỉ dành cho quản lý.
            case 1: // Danh mục
                if ("Khách hàng".equals(action))
                    return "BAN_HANG".equals(vaiTro);
                if ("Nhà cung cấp".equals(action))
                    return vaiTroThuocNhom(vaiTro, "BAN_HANG", "CHAM_SOC", "KHO");
                if ("Nhập hàng".equals(action))
                    return "KHO".equals(vaiTro);
                if ("Lịch làm việc".equals(action))
                    return vaiTroThuocNhom(vaiTro, "BAN_HANG", "CHAM_SOC", "KHO");
                return false;
            case 2: // Cập nhật
                if ("Thú cưng".equals(action))
                    return vaiTroThuocNhom(vaiTro, "BAN_HANG", "CHAM_SOC");
                if ("Dịch vụ".equals(action))
                    return "CHAM_SOC".equals(vaiTro);
                if ("Lịch hẹn".equals(action))
                    return "BAN_HANG".equals(vaiTro);
                if ("Sản phẩm".equals(action))
                    return "KHO".equals(vaiTro);
                return false; // Khuyến mãi chỉ dành cho quản lý.
            case 3: // Xử lý
                if ("Tiếp nhận thú cưng".equals(action))
                    return vaiTroThuocNhom(vaiTro, "BAN_HANG", "CHAM_SOC");
                if ("Lập hóa đơn".equals(action) || "Xuất hóa đơn".equals(action))
                    return "BAN_HANG".equals(vaiTro);
                if ("Cảnh báo tồn kho".equals(action))
                    return vaiTroThuocNhom(vaiTro, "BAN_HANG", "KHO");
                return false; // Xếp lịch làm việc chỉ dành cho quản lý.
            case 4: // Tìm kiếm
                if ("Sản phẩm".equals(action))
                    return vaiTroThuocNhom(vaiTro, "BAN_HANG", "KHO");
                if ("Khách hàng".equals(action) || "Dịch vụ".equals(action))
                    return vaiTroThuocNhom(vaiTro, "BAN_HANG", "CHAM_SOC");
                if ("Lịch hẹn".equals(action))
                    return vaiTroThuocNhom(vaiTro, "BAN_HANG", "CHAM_SOC");
                if ("Tích điểm".equals(action) || "Hóa đơn".equals(action))
                    return "BAN_HANG".equals(vaiTro);
                if ("Phiếu nhập".equals(action))
                    return "KHO".equals(vaiTro);
                return false; // Tra cứu nhân viên chỉ dành cho quản lý.
            default: // Báo cáo và thống kê chỉ dành cho quản lý.
                return false;
        }
    }

    private boolean vaiTroThuocNhom(String vaiTro, String... cacVaiTro) {
        for (String vaiTroDuocPhep : cacVaiTro) {
            if (vaiTroDuocPhep.equals(vaiTro))
                return true;
        }
        return false;
    }

    // Mục con trong menu thả xuống
    private JMenuItem createMenuItem(String text, int menuIndex) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        item.setForeground(TEXT);
        item.setBackground(WHITE);
        item.setBorder(new EmptyBorder(8, 12, 8, 12));
        item.setIconTextGap(10);
        KeyStroke ks = SHORTCUTS.get(text);
        if (ks != null)
            item.setAccelerator(ks);
        item.addActionListener(e -> showAction(text, menuIndex));
        return item;
    }

    // Xử lý khi chọn chức năng trên menu
    private void showAction(String action, int menuIndex) {
        String title = action;
        String cardName = action;

        // Đăng xuất / thoát ứng dụng
        if ("Đăng xuất tài khoản".equals(action)) {
            logout();
            return;
        }
        if ("Thoát ứng dụng".equals(action)) {
            exitApp();
            return;
        }

        if ("Đổi mật khẩu tài khoản".equals(action)) {
            if (!Auth.isLogin() || Auth.user.getMaTK() == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để đổi mật khẩu.",
                        "Chưa đăng nhập", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new dlgDoiMatKhau(this).setVisible(true);
            return;
        }
        if (!coQuyen(action, menuIndex)) {
            JOptionPane.showMessageDialog(this,
                    "Vai trò của tài khoản hiện tại không có quyền truy cập chức năng này.",
                    "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Các mục menu có giao diện hiện có được mở trên màn hình tương ứng.
        if ("Thanh toán".equals(action)) {
            cardName = "Lập hóa đơn";
            title = "Lập hóa đơn";
        } else if ("Bán hàng và dịch vụ".equals(action)) {
            cardName = "Lập hóa đơn";
            title = "Lập hóa đơn";
        } else if ("Đặt lịch dịch vụ".equals(action)) {
            cardName = "Lịch hẹn";
        } else if ("Đơn hàng".equals(action)) {
            cardName = "Hóa đơn";
        } else if ("Nhập hàng".equals(action)) {
            if (menuIndex == 5) {
                cardName = "Báo biểu nhập hàng";
                title = "Thống kê nhập hàng";
            } else {
                cardName = "Nhập hàng";
            }
        } else if ("Phiếu nhập".equals(action) || "Tìm kiếm phiếu nhập".equals(action)) {
            cardName = "Tìm kiếm phiếu nhập";
        } else if ("Cảnh báo tồn kho".equals(action) || "Tồn kho".equals(action)) {
            cardName = "Tồn kho";
        } else if ("Doanh thu".equals(action)) {
            cardName = "Báo biểu doanh thu";
            title = "Thống kê doanh thu";
        } else if ("Lợi nhuận".equals(action)) {
            cardName = "Thống kê lợi nhuận";
            title = "Thống kê lợi nhuận";
        } else if ("Thống kê doanh thu".equals(action) || "Sản phẩm bán chạy".equals(action)) {
            cardName = "Trang chủ";
        } else if ("Phân quyền vai trò".equals(action)) {
            cardName = "Nhân viên";
        }

        // Hiển thị giao diện chức năng
        if (!registeredCards.contains(cardName)) {
            registerCard(cardName, createPage(cardName, title));
        }

        if ("Nhân viên".equals(cardName) && employeePanel != null) {
            employeePanel.chonTab("Phân quyền vai trò".equals(action) ? "Phân quyền" : "Nhân viên");
        }
        pageTitle.setText(title);
        btnLamMoiTrangChu.setVisible("Trang chủ".equals(cardName));
        cardLayout.show(cardPanel, cardName);
        if ("Lịch làm việc".equals(cardName) && xemLichPanel != null) {
            xemLichPanel.taiLich();
        }

        if ("Xếp lịch làm việc".equals(cardName) && xepLichPanel != null) {
            xepLichPanel.lamMoi();
        }
    }

    private JPanel createPage(String cardName, String title) {
        if ("Nhà cung cấp".equals(cardName))
            return new pnlNhaCungCap();
        if ("Khuyến mãi".equals(cardName))
            return new pnlKhuyenMai();
        if ("Nhập hàng".equals(cardName))
            return new pnlNhapHang();
        if ("Tìm kiếm phiếu nhập".equals(cardName))
            return new pnlTimKiemPhieuNhap();
        if ("Báo biểu nhập hàng".equals(cardName))
            return new pnlBaoBieuNhapHang();
        if ("Tiếp nhận thú cưng".equals(cardName))
            return new pnlTiepNhanThuCung();
        if ("Báo biểu doanh thu".equals(cardName))
            return new pnlBaoBieuDoanhThu();
        if ("Thống kê lợi nhuận".equals(cardName))
            return new pnlBaoBieuLoiNhuan();
        if ("Tồn kho".equals(cardName))
            return new pnlTonKho();
        return placeholder(title);
    }
    private void registerCard(String name, JPanel page) {
        if (registeredCards.add(name))
            cardPanel.add(page, name);
    }

    // Giữ lại các panel thật trong project
    private JPanel createExistingPanel(String name) {
        try {
            if ("Nhân viên".equals(name))
                return new pnlNhanVien();
            if ("Hóa đơn".equals(name))
                return new pnlQuanLyHoaDon();
            if ("Thanh toán".equals(name))
                return placeholder("Thanh toán");
        } catch (Exception ex) {
            System.err.println("Không thể tạo panel " + name + ": " + ex.getMessage());
        }
        return placeholder(name);
    }

    private JPanel placeholder(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        JLabel message = label("Chức năng " + title + " đang được hoàn thiện", 22, TEXT, true);
        message.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(message, BorderLayout.CENTER);
        return panel;
    }

    // 6. GIAO DIỆN TRANG CHỦ
    private JPanel createHomePage() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(16, 18, 24, 18));
        content.add(createWelcome());
        content.add(Box.createVerticalStrut(16));
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        lblDonHangHomNay = new JLabel("...");
        lblDichVuHomNay = new JLabel("...");
        lblSanPhamHomNay = new JLabel("...");
        lblDoanhThuHomNay = new JLabel("...");
        stats.add(createStat(lblDonHangHomNay,
                "Đơn hàng hôm nay",
                "icon_order.png",
                new Color(255, 242, 247)));
        stats.add(createStat(lblDichVuHomNay,
                "Dịch vụ trong ngày",
                "icon_service.png",
                new Color(239, 247, 255)));
        stats.add(createStat(lblSanPhamHomNay,
                "Sản phẩm bán ra",
                "icon_product.png",
                new Color(239, 253, 246)));
        stats.add(createStat(lblDoanhThuHomNay,
                "Doanh thu hôm nay",
                "icon_revenue.png",
                new Color(255, 249, 235)));
        content.add(stats);
        content.add(Box.createVerticalStrut(16));
        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 0));
        charts.setOpaque(false);
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        // Biểu đồ doanh thu
        revenueChart = new RevenueChart();
        charts.add(createSection(
                "Doanh thu 7 ngày gần nhất",
                "chart.png",
                revenueChart));
        // Sản phẩm bán chạy
        charts.add(createSection(
                "Sản phẩm bán chạy",
                "crown.png",
                createTopProducts()));
        content.add(charts);
        content.add(Box.createVerticalStrut(16));
        // Đơn hàng gần đây
        JPanel recent = createSection(
                "Đơn hàng gần đây",
                "cart.png",
                createRecentOrders());
        recent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        content.add(recent);
        content.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // Banner chào mừng
    private JPanel createWelcome() {
        RoundPanel banner = new RoundPanel(new Color(255, 240, 246), 18);
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(20, 28, 20, 28));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        titles.add(label("Xin chào!", 28, PRIMARY_DARK, true));
        titles.add(label("Chúc bạn một ngày làm việc hiệu quả.", 15, MUTED, false));
        banner.add(titles, BorderLayout.WEST);
        JLabel petImage = new JLabel();
        ImageIcon petIcon = loadIcon("Corgi.png", 170, 105);
        if (petIcon != null) {
            petImage.setIcon(petIcon);
        }
        petImage.setHorizontalAlignment(SwingConstants.RIGHT);
        banner.add(petImage, BorderLayout.EAST);
        return banner;
    }

    // Các thẻ thống kê lấy dữ liệu từ database
    private JPanel createStat(JLabel valueLabel, String text, String icon, Color color) {
        RoundPanel card = new RoundPanel(color, 15);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 12));
        // Hiển thị icon thống kê
        JLabel image = new JLabel();
        ImageIcon statIcon = loadIcon(icon, 48, 48);
        if (statIcon != null) {
            image.setIcon(statIcon);
        }
        image.setPreferredSize(new Dimension(55, 55));
        JPanel numbers = new JPanel(new GridLayout(2, 1, 0, 4));
        numbers.setOpaque(false);
        valueLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        valueLabel.setForeground(TEXT);
        numbers.add(valueLabel);
        numbers.add(label(text, 13, MUTED, false));
        card.add(image, BorderLayout.WEST);
        card.add(numbers, BorderLayout.CENTER);
        return card;
    }

    private static class DashboardData {
        Object[] todayStats;
        java.util.List<Object[]> revenue;
        java.util.List<Object[]> topProducts;
        java.util.List<Object[]> recentOrders;
    }

    // Tải lại toàn bộ dữ liệu Dashboard trong một worker để giao diện không bị treo.
    private void refreshDashboard() {
        if (btnLamMoiTrangChu != null) {
            btnLamMoiTrangChu.setEnabled(false);
            btnLamMoiTrangChu.setText("Đang tải...");
        }
        SwingWorker<DashboardData, Void> worker = new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardData data = new DashboardData();
                HoaDonDAO hoaDonDAO = new HoaDonDAO();
                data.todayStats = hoaDonDAO.layThongKeHomNay();
                data.revenue = hoaDonDAO.layDoanhThu7Ngay();
                data.topProducts = new SanPhamDAO().layTop5SanPhamBanChay();
                data.recentOrders = hoaDonDAO.layHoaDonGanDay();
                return data;
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    Object[] stats = data.todayStats;
                    lblDonHangHomNay.setText(String.valueOf(stats[0]));
                    lblDichVuHomNay.setText(String.valueOf(stats[1]));
                    lblSanPhamHomNay.setText(String.valueOf(stats[2]));
                    java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance(
                            new java.util.Locale("vi", "VN"));
                    lblDoanhThuHomNay.setText(format.format(stats[3]) + " đ");
                    revenueChart.setData(data.revenue);
                    populateTopProducts(data.topProducts);
                    populateRecentOrders(data.recentOrders);
                } catch (Exception e) {
                    System.err.println("Lỗi tải dữ liệu Dashboard: " + e.getMessage());
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Không tải được dữ liệu trang chủ từ SQL Server. Vui lòng thử làm mới lại.",
                            "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    btnLamMoiTrangChu.setEnabled(true);
                    btnLamMoiTrangChu.setText("Làm mới");
                }
            }
        };
        worker.execute();
    }

    // Tạo khung nội dung có icon
    private JPanel createSection(
            String heading,
            String iconName,
            JComponent body) {
        RoundPanel panel = new RoundPanel(WHITE, 14);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(13, 15, 14, 15)));
        // Tiêu đề và icon
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        JLabel iconLabel = new JLabel();
        ImageIcon icon = loadIcon(iconName, 26, 26);
        if (icon != null) {
            iconLabel.setIcon(icon);
        }
        JLabel title = label(
                heading, 17, TEXT, true);
        header.add(iconLabel);
        header.add(title);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    // Hiển thị Top 5 sản phẩm bán chạy
    private JPanel createTopProducts() {
        JPanel list = new JPanel(new GridLayout(0, 1, 0, 5));
        list.setOpaque(false);
        topProductsList = list;
        return list;
    }

    private void populateTopProducts(java.util.List<Object[]> products) {
        topProductsList.removeAll();
        if (products.isEmpty()) {
            topProductsList.add(label("Chưa có sản phẩm được bán", 13, MUTED, false));
        }
        for (int i = 0; i < products.size(); i++) {
            Object[] item = products.get(i);
            JPanel row = new JPanel(new BorderLayout(9, 0));
            row.setOpaque(false);
            JLabel num = label(String.valueOf(i + 1), 13, PRIMARY, true);
            num.setPreferredSize(new Dimension(25, 26));
            JLabel name = label(String.valueOf(item[0]), 13, TEXT, false);
            JLabel quantity = label(String.valueOf(item[1]), 13, TEXT, false);
            row.add(num, BorderLayout.WEST);
            row.add(name, BorderLayout.CENTER);
            row.add(quantity, BorderLayout.EAST);
            topProductsList.add(row);
        }
        topProductsList.revalidate();
        topProductsList.repaint();
    }

    private JScrollPane createRecentOrders() {
        String[] columns = { "Mã đơn", "Khách hàng", "Tổng tiền", "Trạng thái", "Thời gian" };
        // Khởi tạo bảng rỗng, dữ liệu sẽ lấy từ database
        Object[][] rows = {};
        recentOrdersModel = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(recentOrdersModel);
        // Căn lề trái cho dữ liệu trong bảng
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, column);
                cell.setHorizontalAlignment(SwingConstants.LEFT);
                cell.setBorder(new EmptyBorder(0, 20, 0, 5));
                return cell;
            }
        });
        table.setRowHeight(36);
        table.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setGridColor(new Color(242, 242, 246));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(255, 225, 235));
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setFont(new Font("Times New Roman", Font.BOLD, 13));
        header.setBackground(new Color(246, 247, 250));
        header.setForeground(TEXT);
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                // Căn giữa nội dung cột Trạng thái
                cell.setHorizontalAlignment(SwingConstants.CENTER);
                cell.setBorder(new EmptyBorder(0, 0, 0, 0));
                String status = String.valueOf(value);
                cell.setForeground(status.equals("Đã thanh toán") ? new Color(0, 155, 100)
                        : status.equals("Đã hủy") ? new Color(215, 48, 70) : new Color(215, 128, 0));
                cell.setBackground(selected ? t.getSelectionBackground() : WHITE);
                return cell;
            }
        });
        // Điều chỉnh chiều cao bảng theo số dòng dữ liệu
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(null);
        int rowHeight = table.getRowHeight();
        int headerHeight = table.getTableHeader().getPreferredSize().height;
        // Ban đầu hiển thị chiều cao của phần tiêu đề
        pane.setPreferredSize(new Dimension(0, headerHeight + rowHeight));
        // Tự cập nhật chiều cao khi dữ liệu thay đổi
        recentOrdersModel.addTableModelListener(e -> {
            int rowCount = recentOrdersModel.getRowCount();
            int visibleRows = Math.max(1, Math.min(rowCount, 10));
            int height = headerHeight + visibleRows * rowHeight + 2;
            pane.setPreferredSize(new Dimension(0, height));
            pane.revalidate();
        });
        return pane;
    }

    private void populateRecentOrders(java.util.List<Object[]> orders) {
        recentOrdersModel.setRowCount(0);
        for (Object[] row : orders) {
            recentOrdersModel.addRow(new Object[] {
                    row[0], row[1],
                    String.format(new Locale("vi", "VN"), "%,.0f đ", row[2]),
                    row[3], row[4]
            });
        }
    }

    // Biểu đồ doanh thu 7 ngày gần nhất
    private static class RevenueChart extends JPanel {
        private final double[] values = new double[7];
        private final String[] days = new String[7];
        private boolean loading = true;
        private String errorMessage = null;
        // Trục Y cố định từ 0 đến 4 triệu
        private static final double MAX_REVENUE = 4_000_000.0;

        RevenueChart() {
            setOpaque(false);
        }

        void setData(java.util.List<Object[]> data) {
            Arrays.fill(values, 0);
            Arrays.fill(days, null);
            errorMessage = null;
            loading = false;
            for (int i = 0; i < data.size() && i < 7; i++) {
                Object[] row = data.get(i);
                java.time.LocalDate date = (java.time.LocalDate) row[0];
                days[i] = date.format(DateTimeFormatter.ofPattern("dd/MM"));
                values[i] = ((Number) row[1]).doubleValue();
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            if (loading || errorMessage != null) {
                g.setColor(MUTED);
                g.setFont(new Font("Times New Roman", Font.PLAIN, 14));
                g.drawString(
                        loading ? "Đang tải doanh thu..." : errorMessage,
                        20, Math.max(20, height / 2));
                g.dispose();
                return;
            }
            int left = 48;
            int right = 15;
            int top = 15;
            int bottom = height - 30;
            int chartHeight = Math.max(1, bottom - top);
            int chartWidth = Math.max(1, width - left - right);
            // Vẽ trục Y cố định 0, 1M, 2M, 3M, 4M
            g.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            for (int i = 0; i <= 4; i++) {
                int y = bottom - i * chartHeight / 4;
                g.setColor(new Color(232, 235, 241));
                g.drawLine(left, y, width - right, y);
                g.setColor(MUTED);
                String label = i == 0 ? "0" : i + "M";
                g.drawString(label, 10, y + 4);
            }
            // Vẽ các cột theo tỷ lệ doanh thu thực tế
            int slot = chartWidth / 7;
            for (int i = 0; i < 7; i++) {
                int barWidth = Math.max(6, Math.min(48, slot - 14));
                int x = left + i * slot + (slot - barWidth) / 2;
                // Cột tỷ lệ theo trục Y
                int barHeight = (int) Math.round(
                        Math.max(0, Math.min(values[i], MAX_REVENUE))
                                / MAX_REVENUE * chartHeight);
                g.setColor(new Color(255, 143, 181));
                g.fillRoundRect(
                        x,
                        bottom - barHeight,
                        barWidth,
                        barHeight,
                        6,
                        6);
                // Hiển thị ngày dưới mỗi cột
                if (days[i] != null) {
                    g.setColor(MUTED);
                    g.setFont(new Font(
                            "Times New Roman", Font.PLAIN, 11));
                    g.drawString(days[i], x - 3, height - 8);
                }
            }
            g.dispose();
        }
    }

    // Các thành phần giao diện dùng lại
    private ImageIcon loadIcon(String name, int w, int h) {
        URL url = getClass().getResource("/icons/" + name);
        if (url == null) {
            url = getClass().getResource("/resources/icons/" + name);
        }
        if (url == null) {
            java.nio.file.Path directory = java.nio.file.Paths.get(System.getProperty("user.dir"))
                    .toAbsolutePath().normalize();
            while (directory != null && url == null) {
                java.nio.file.Path[] candidates = {
                        directory.resolve("resources").resolve("icons").resolve(name),
                        directory.resolve("pet").resolve("PetPlus").resolve("resources").resolve("icons")
                                .resolve(name)
                };
                for (java.nio.file.Path candidate : candidates) {
                    if (java.nio.file.Files.isRegularFile(candidate)) {
                        try {
                            url = candidate.toUri().toURL();
                        } catch (java.net.MalformedURLException ex) {
                            System.err.println("Không thể mở icon " + name + ": " + ex.getMessage());
                        }
                        break;
                    }
                }
                directory = directory.getParent();
            }
        }
        if (url == null) {
            System.err.println("Không tìm thấy icon: " + name);
            return null;
        }
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private JLabel label(String text, int size, Color color, boolean bold) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("Times New Roman", bold ? Font.BOLD : Font.PLAIN, size));
        result.setForeground(color);
        return result;
    }

    private void updateDate() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        lblDate.setText(date);
    }

    // Đăng xuất và quay về màn hình đăng nhập
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            Auth.clear();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    // Thoát ứng dụng
    private void exitApp() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn thoát ứng dụng?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION)
            System.exit(0);
    }

    private static class RoundPanel extends JPanel {
        private final Color color;
        private final int radius;

        RoundPanel(Color color, int radius) {
            this.color = color;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

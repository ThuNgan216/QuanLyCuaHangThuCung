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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import gui.panel.pnlHoaDon;
import gui.panel.pnlThuCung;
import gui.panel.pnlTichDiem;
import gui.panel.pnlDichVu;
import gui.panel.pnlLichHen;
import gui.panel.pnlKhachHang;
import gui.panel.pnlQuanLyHoaDon;
import gui.panel.pnlNhanVien;
import utils.Auth;
import dao.HoaDonDAO;
import dao.SanPhamDAO;

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
    private JPanel sidebarPanel;
    private JPanel menuPanel;
    private JPanel toolbarPanel;
    private JPanel homePage;
    private JLabel lblName;
    private JLabel lblRole;
    private JLabel lblDate;
    // Các chỉ số thống kê trên Dashboard
    private JLabel lblDonHangHomNay;
    private JLabel lblDichVuHomNay;
    private JLabel lblSanPhamHomNay;
    private JLabel lblDoanhThuHomNay;
    private JButton activeButton;
    private final String[] MENU_ITEMS = { "Trang chủ", "Hệ thống", "Danh mục", "Cập nhật", "Tìm kiếm", "Xử lý",
            "Báo biểu" };
    private final String[] MENU_ICONS = { "home.png", "settings.png", "box.png", "employee.png", "search.png",
            "cart.png", "chart.png" };
    // Menu con theo từng nhóm chức năng
    private final String[][] SUB_MENUS = {
            {},
            // Hệ thống
            { "Đổi mật khẩu", "Cài đặt" },
            // Danh mục
            { "Loại sản phẩm", "Nhà cung cấp", "Dịch vụ", "Ca làm việc" },
            // Cập nhật
            { "Sản phẩm", "Khách hàng", "Thú cưng", "Tích điểm", "Nhân viên", "Lịch hẹn", "Khuyến mãi", "Quản lý hóa đơn" },
            // Tìm kiếm
            { "Sản phẩm", "Khách hàng", "Hóa đơn", "Phiếu nhập", "Lịch hẹn" },
            // Xử lý
            { "Tạo đơn hàng", "Thanh toán", "Nhập hàng", "Quét mã vạch", "Đặt lịch dịch vụ" },
            // Báo biểu
            { "Thống kê doanh thu", "Tồn kho", "Sản phẩm bán chạy", "Báo cáo nhập hàng" }
    };
    private final JButton[] menuButtons = new JButton[MENU_ITEMS.length];
    private final java.util.Set<String> registeredCards = new HashSet<>();

    public MainFrame() {
        setTitle("PetPlus - Quản lý cửa hàng thú cưng");
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
        // Tải dữ liệu thống kê khi mở Dashboard
        loadDashboardStats();
        registerCard("Trang chủ", homePage);
        // 2. THANH SIDEBAR
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setBackground(new Color(255, 247, 250));
        sidebarPanel.setPreferredSize(new Dimension(230, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));
        // 2.1 TOP SIDEBAR: Logo & Tên cửa hàng
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(new EmptyBorder(20, 18, 25, 18));
        // Cột trái: Icon Logo Paw
        JLabel lblIcon = new JLabel();
        URL pawIconUrl = getClass().getResource("/icons/paw.png");
        if (pawIconUrl != null) {
            ImageIcon originalIcon = new ImageIcon(pawIconUrl);
            Image scaledImage = originalIcon.getImage()
                    .getScaledInstance(45, 45, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(scaledImage));
        }
        // Cột phải: Logo & Tên cửa hàng
        JPanel logoText = new JPanel();
        logoText.setOpaque(false);
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));
        JLabel logoTitle = label("PetPlus", 27, PRIMARY_DARK, true);
        JLabel logoSub = label("Quản lý cửa hàng thú cưng", 11, MUTED, false);
        logoText.add(logoTitle);
        logoText.add(Box.createVerticalStrut(3));
        logoText.add(logoSub);
        // Ráp Logo và Cột Text vào Top Panel
        topPanel.add(lblIcon);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(logoText);
        sidebarPanel.add(topPanel, BorderLayout.NORTH);
        // 2.2 CENTER: Menu hướng chức năng
        menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(new EmptyBorder(6, 13, 10, 13));
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            final int index = i;
            JButton button = createSidebarButton(MENU_ITEMS[i], MENU_ICONS[i]);
            menuButtons[i] = button;
            menuPanel.add(button);
            menuPanel.add(Box.createVerticalStrut(8));
            button.addActionListener(e -> selectMenu(index));
        }
        JPanel menuHolder = new JPanel(new BorderLayout());
        menuHolder.setOpaque(false);
        menuHolder.add(menuPanel, BorderLayout.NORTH);
        sidebarPanel.add(new JScrollPane(menuHolder,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            {
                setBorder(null);
                setOpaque(false);
                getViewport().setOpaque(false);
            }
        }, BorderLayout.CENTER);
        // 2.3 BOTTOM SIDEBAR: Thông tin User & Đăng xuất
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(14, 15, 21, 15));
        bottomPanel.add(new JSeparator(), BorderLayout.NORTH);
        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setOpaque(false);
        // Avatar tài khoản hình tròn
        JLabel avatar = new JLabel("A", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Vẽ nền hình tròn màu hồng
                g2.setColor(PRIMARY);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                // Vẽ chữ A lên trên hình tròn
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(48, 48));
        avatar.setOpaque(false);
        avatar.setForeground(WHITE);
        avatar.setFont(new Font("Times New Roman", Font.BOLD, 20));
        JPanel profileText = new JPanel(new GridLayout(2, 1, 0, 3));
        profileText.setOpaque(false);
        lblName = label("Chưa đăng nhập", 14, TEXT, true);
        lblRole = label("", 12, MUTED, false);
        profileText.add(lblName);
        profileText.add(lblRole);
        profile.add(avatar, BorderLayout.WEST);
        profile.add(profileText, BorderLayout.CENTER);
        JButton logout = new JButton("Đăng xuất");
        logout.setBackground(WHITE);
        logout.setForeground(PRIMARY);
        logout.setFont(new Font("Times New Roman", Font.BOLD, 14));
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true), new EmptyBorder(10, 12, 10, 12)));
        logout.addActionListener(e -> logout());
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        footer.add(profile, BorderLayout.NORTH);
        footer.add(logout, BorderLayout.SOUTH);
        bottomPanel.add(footer, BorderLayout.CENTER);
        sidebarPanel.add(bottomPanel, BorderLayout.SOUTH);
        // 3. THANH NGANG CHỨC NĂNG
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setBackground(BG);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHITE);
        headerPanel.setBorder(new EmptyBorder(12, 18, 7, 22));
        JLabel pageTitle = label("Trang chủ", 19, TEXT, true);
        // Icon lịch ngày giờ
        lblDate = label("", 13, TEXT, false);
        ImageIcon calendarIcon = loadIcon("Calendar.png", 24, 24);
        if (calendarIcon != null) {
            lblDate.setIcon(calendarIcon);
            lblDate.setIconTextGap(10);
        }
        headerPanel.add(pageTitle, BorderLayout.WEST);
        headerPanel.add(lblDate, BorderLayout.EAST);
        toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbarPanel.setBackground(new Color(255, 243, 247));
        toolbarPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerPanel, BorderLayout.NORTH);
        headerWrapper.add(toolbarPanel, BorderLayout.CENTER);
        rightSide.add(headerWrapper, BorderLayout.NORTH);
        rightSide.add(cardPanel, BorderLayout.CENTER);
        add(sidebarPanel, BorderLayout.WEST);
        add(rightSide, BorderLayout.CENTER);
        // 4. KHỞI TẠO CÁC PANEL CÓ SẴN
        registerCard("Nhân viên", createExistingPanel("Nhân viên"));
        registerCard("Thanh toán", createExistingPanel("Thanh toán"));
        // Đăng ký trang quản lý hóa đơn
        registerCard("Quản lý hóa đơn", new pnlHoaDon());
        registerCard("Hóa đơn", createExistingPanel("Hóa đơn"));
        registerCard("Khách hàng", new pnlKhachHang());
        registerCard("Thú cưng", new pnlThuCung());
        registerCard("Tích điểm", new pnlTichDiem());
        registerCard("Dịch vụ", new pnlDichVu());
        registerCard("Lịch hẹn", new pnlLichHen());
        registerCard("Đổi mật khẩu", placeholder("Đổi mật khẩu"));
        // 5. HIỂN THỊ THÔNG TIN TÀI KHOẢN
        updateAccountInfo();
        selectMenu(0);
        javax.swing.Timer clock = new javax.swing.Timer(60000, e -> updateDate());
        clock.start();
        updateDate();
    }
    // Hiển thị thông tin người đăng nhập, không kiểm tra phân quyền

    private void updateAccountInfo() {
        if (Auth.user != null) {
            lblName.setText(Auth.user.getTenNV());
            lblRole.setText(Auth.user.getMaTK().getVaiTro());
        } else {
            lblName.setText("Dev Test");
            lblRole.setText("Chưa đăng nhập");
        }
    }
    // Điều hướng menu bên trái và menu ngang

    private void selectMenu(int index) {
        setActiveButton(menuButtons[index]);
        toolbarPanel.removeAll();
        if (index == 0) {
            String[] shortcuts = { "Tạo đơn hàng", "Thanh toán", "Nhập hàng", "Quét mã vạch", "Đặt lịch dịch vụ" };
            for (String action : shortcuts) {
                toolbarPanel.add(createToolbarButton(action));
            }
            cardLayout.show(cardPanel, "Trang chủ");
        } else {
            for (String action : SUB_MENUS[index]) {
                toolbarPanel.add(createToolbarButton(action));
            }
            if (toolbarPanel.getComponentCount() > 0) {
                JButton first = (JButton) toolbarPanel.getComponent(0);
                showAction(first.getText());
            }
        }
        toolbarPanel.revalidate();
        toolbarPanel.repaint();
    }

    private JButton createToolbarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBackground(WHITE);
        btn.setForeground(TEXT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(10, 17, 10, 17)));
        btn.addActionListener(e -> showAction(text));
        return btn;
    }

    private void showAction(String action) {
        if (!registeredCards.contains(action))
            registerCard(action, placeholder(action));
        cardLayout.show(cardPanel, action);
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
        charts.add(createSection(
                "Doanh thu 7 ngày gần nhất",
                "icon_revenue_chart.png",
                new RevenueChart()));
        // Sản phẩm bán chạy
        charts.add(createSection(
                "Sản phẩm bán chạy",
                "icon_best_selling.png",
                createTopProducts()));
        content.add(charts);
        content.add(Box.createVerticalStrut(16));
        // Đơn hàng gần đây
        JPanel recent = createSection(
                "Đơn hàng gần đây",
                "icon_recent_orders.png",
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
    // Tải 4 chỉ số thống kê từ SQL Server

    private void loadDashboardStats() {
        SwingWorker<Object[], Void> worker = new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground()
                    throws Exception {
                HoaDonDAO dao = new HoaDonDAO();
                return dao.layThongKeHomNay();
            }

            @Override
            protected void done() {
                try {
                    Object[] data = get();
                    lblDonHangHomNay.setText(
                            String.valueOf(data[0]));
                    lblDichVuHomNay.setText(
                            String.valueOf(data[1]));
                    lblSanPhamHomNay.setText(
                            String.valueOf(data[2]));
                    java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance(
                            new java.util.Locale("vi", "VN"));
                    lblDoanhThuHomNay.setText(
                            format.format(data[3]) + " đ");
                } catch (Exception e) {
                    System.err.println(
                            "Lỗi tải thống kê Dashboard: "
                                    + e.getMessage());
                    lblDonHangHomNay.setText("—");
                    lblDichVuHomNay.setText("—");
                    lblSanPhamHomNay.setText("—");
                    lblDoanhThuHomNay.setText("—");
                    e.printStackTrace();
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
        // Tải thống kê từ database trên luồng nền
        SwingWorker<java.util.List<Object[]>, Void> worker = new SwingWorker<java.util.List<Object[]>, Void>() {
            @Override
            protected java.util.List<Object[]> doInBackground()
                    throws Exception {
                SanPhamDAO dao = new SanPhamDAO();
                return dao.layTop5SanPhamBanChay();
            }

            @Override
            protected void done() {
                try {
                    java.util.List<Object[]> danhSach = get();
                    list.removeAll();
                    if (danhSach.isEmpty()) {
                        list.add(label(
                                "Chưa có sản phẩm được bán",
                                13, MUTED, false));
                    }
                    for (int i = 0; i < danhSach.size(); i++) {
                        Object[] item = danhSach.get(i);
                        JPanel row = new JPanel(
                                new BorderLayout(9, 0));
                        row.setOpaque(false);
                        JLabel num = label(
                                String.valueOf(i + 1),
                                13, PRIMARY, true);
                        num.setPreferredSize(new Dimension(25, 26));
                        JLabel name = label(
                                String.valueOf(item[0]),
                                13, TEXT, false);
                        JLabel quantity = label(
                                String.valueOf(item[1]),
                                13, TEXT, false);
                        row.add(num, BorderLayout.WEST);
                        row.add(name, BorderLayout.CENTER);
                        row.add(quantity, BorderLayout.EAST);
                        list.add(row);
                    }
                    list.revalidate();
                    list.repaint();
                } catch (Exception e) {
                    list.removeAll();
                    list.add(label(
                            "Không tải được thống kê sản phẩm",
                            13, PRIMARY_DARK, false));
                    list.revalidate();
                    list.repaint();
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
        return list;
    }

    private JScrollPane createRecentOrders() {
        String[] columns = { "Mã đơn", "Khách hàng", "Tổng tiền", "Trạng thái", "Thời gian" };
        // Khởi tạo bảng rỗng, dữ liệu sẽ lấy từ database
        Object[][] rows = {};
        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(model);
        // Căn lề trái 5px cho dữ liệu trong bảng
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
        // Tải hóa đơn gần đây từ database trên luồng nền
        SwingWorker<java.util.List<Object[]>, Void> worker = new SwingWorker<java.util.List<Object[]>, Void>() {
            @Override
            protected java.util.List<Object[]> doInBackground()
                    throws Exception {
                HoaDonDAO dao = new HoaDonDAO();
                return dao.layHoaDonGanDay();
            }

            @Override
            protected void done() {
                try {
                    java.util.List<Object[]> danhSach = get();
                    model.setRowCount(0);
                    for (Object[] row : danhSach) {
                        model.addRow(new Object[] {
                                row[0],
                                row[1],
                                String.format(new Locale("vi", "VN"), "%,.0f đ", row[2]),
                                row[3],
                                row[4]
                        });
                    }
                } catch (Exception e) {
                    System.err.println(
                            "Không tải được hóa đơn gần đây: "
                                    + e.getMessage());
                    JOptionPane.showMessageDialog(
                            MainFrame.this,
                            "Không tải được hóa đơn gần đây từ SQL Server.",
                            "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
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
        model.addTableModelListener(e -> {
            int rowCount = model.getRowCount();
            int visibleRows = Math.max(1, Math.min(rowCount, 10));
            int height = headerHeight + visibleRows * rowHeight + 2;
            pane.setPreferredSize(new Dimension(0, height));
            pane.revalidate();
        });
        return pane;
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
            loadRevenue();
        }

        // Tải dữ liệu doanh thu từ database
        private void loadRevenue() {
            SwingWorker<java.util.List<Object[]>, Void> worker = new SwingWorker<java.util.List<Object[]>, Void>() {
                @Override
                protected java.util.List<Object[]> doInBackground()
                        throws Exception {
                    HoaDonDAO dao = new HoaDonDAO();
                    return dao.layDoanhThu7Ngay();
                }

                @Override
                protected void done() {
                    try {
                        java.util.List<Object[]> data = get();
                        for (int i = 0; i < data.size() && i < 7; i++) {
                            Object[] row = data.get(i);
                            java.time.LocalDate date = (java.time.LocalDate) row[0];
                            days[i] = date.format(
                                    DateTimeFormatter.ofPattern("dd/MM"));
                            values[i] = ((Number) row[1]).doubleValue();
                        }
                    } catch (Exception e) {
                        errorMessage = "Không tải được doanh thu";
                        e.printStackTrace();
                    } finally {
                        loading = false;
                        repaint();
                    }
                }
            };
            worker.execute();
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

    private JButton createSidebarButton(String text, String iconName) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(236, 50));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBackground(new Color(255, 247, 250));
        btn.setForeground(TEXT);
        btn.setFont(new Font("Times New Roman", Font.BOLD, 15));
        btn.setBorder(new EmptyBorder(8, 16, 8, 8));
        btn.setIconTextGap(16);
        ImageIcon icon = loadIcon(iconName, 30, 30);
        if (icon != null)
            btn.setIcon(icon);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton)
                    btn.setBackground(new Color(255, 226, 237));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton)
                    btn.setBackground(new Color(255, 247, 250));
            }
        });
        return btn;
    }

    private void setActiveButton(JButton clickedBtn) {
        for (JButton button : menuButtons) {
            if (button != null) {
                button.setBackground(new Color(255, 247, 250));
                button.setForeground(TEXT);
            }
        }
        activeButton = clickedBtn;
        if (clickedBtn != null) {
            clickedBtn.setBackground(PRIMARY);
            clickedBtn.setForeground(WHITE);
        }
    }

    private ImageIcon loadIcon(String name, int w, int h) {
        URL url = getClass().getResource("/icons/" + name);
        if (url == null)
            return null;
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

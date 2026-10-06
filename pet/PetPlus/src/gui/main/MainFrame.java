package gui.main; // Nhớ đổi đúng package

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import appColor.AppColors;
import utils.Auth; // Gọi class Auth để kiểm tra quyền

public class MainFrame extends JFrame {
	private JPanel cardPanel;
	private CardLayout cardLayout;
	private JPanel sidebarPanel;
	private JPanel menuPanel;
	private JPanel homePage;

	// Labels chứa thông tin user
	private JLabel lblName;
	private JLabel lblRole;

	private final String[] MENU_ITEMS = { "Trang chủ", "Quản lý nhân viên", "Quản lý sản phẩm & kho",
			"Quản lý thú cưng", "Quản lý khách hàng", "Quản lý bán hàng & thanh toán", "Quản lý dịch vụ & lịch hẹn",
			"Thống kê và báo cáo" };

	private final String[] MENU_ICONS = { "home.png", "employee.png", "box.png", "pet.png", "customer.png", "cart.png",
			"calendar.png", "chart.png" };

	// Mảng lưu trữ các nút menu để dễ dàng bật/tắt theo Role
	private JButton[] menuButtons = new JButton[MENU_ITEMS.length];
	private JButton activeButton = null; // Biến lưu nút đang được chọn

	public MainFrame() {
		setTitle("PetPlus - Quản lý cửa hàng thú cưng");
		setSize(1280, 768);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// --- 1. VÙNG NỘI DUNG CHÍNH ---
		cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);
		cardPanel.setBackground(AppColors.BACKGROUND);

		// --- 2. THANH SIDEBAR ---
		sidebarPanel = new JPanel(new BorderLayout());
		sidebarPanel.setBackground(AppColors.SIDEBAR);
		sidebarPanel.setPreferredSize(new Dimension(320, getHeight()));
		sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppColors.BORDER));

		// 2.1 TOP SIDEBAR: Logo & Tên cửa hàng
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS)); // Ép xếp ngang
		topPanel.setBackground(AppColors.SIDEBAR);
		topPanel.setBorder(new EmptyBorder(25, 20, 30, 20));

		// Cột trái: Icon Logo
		JLabel lblIcon = new JLabel();
		try {
			URL pawIconUrl = getClass().getResource("/icons/paw.png");
			if (pawIconUrl != null) {
				ImageIcon originalIcon = new ImageIcon(pawIconUrl);
				// Resize icon về 45x45 cho vừa vặn với thanh Sidebar
				Image scaledImage = originalIcon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
				lblIcon.setIcon(new ImageIcon(scaledImage));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Cột phải: Chứa 2 dòng text xếp dọc
		JPanel pnlText = new JPanel();
		pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
		pnlText.setBackground(AppColors.SIDEBAR);

		JLabel logoTitle = new JLabel("PetPlus");
		logoTitle.setFont(new Font("Arial", Font.BOLD, 28));
		logoTitle.setForeground(AppColors.PRIMARY_DARK);
		logoTitle.setAlignmentX(Component.LEFT_ALIGNMENT); // Ép lề trái

		JLabel logoSub = new JLabel("Quản lý cửa hàng thú cưng");
		logoSub.setFont(new Font("Arial", Font.PLAIN, 13)); // Chữ nhỏ lại xíu cho khỏi tràn viền
		logoSub.setForeground(AppColors.TEXT);
		logoSub.setAlignmentX(Component.LEFT_ALIGNMENT); // Ép lề trái

		pnlText.add(logoTitle);
		pnlText.add(logoSub);

		// Ráp Logo và Cột Text vào Top Panel
		topPanel.add(lblIcon);
		topPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Khoảng cách giữa Logo và Chữ
		topPanel.add(pnlText);

		sidebarPanel.add(topPanel, BorderLayout.NORTH);

		// 2.2 CENTER: Menu
		menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
		menuPanel.setBackground(AppColors.SIDEBAR);

		// Tạo trang chủ
		homePage = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 40));
		homePage.setBackground(AppColors.BACKGROUND);
		homePage.setBorder(new EmptyBorder(20, 20, 20, 20));
		cardPanel.add(homePage, "Trang chủ");

		// Khởi tạo các nút menu và các trang trống
		for (int i = 0; i < MENU_ITEMS.length; i++) {
			String item = MENU_ITEMS[i];

			// Nút trên Sidebar
			menuButtons[i] = createSidebarButton(item, MENU_ICONS[i]);
			menuPanel.add(menuButtons[i]);

			// Form giả định
			if (!item.equals("Trang chủ")) {
				JPanel dummyPanel = new JPanel(new BorderLayout());
				dummyPanel.setBackground(AppColors.BACKGROUND);
				JLabel label = new JLabel(item, SwingConstants.CENTER);
				label.setFont(new Font("Arial", Font.BOLD, 30));
				dummyPanel.add(label, BorderLayout.CENTER);
				cardPanel.add(dummyPanel, item);
			}
		}
		sidebarPanel.add(menuPanel, BorderLayout.CENTER);

		// 2.3 BOTTOM SIDEBAR: Thông tin User & Tùy chọn
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBackground(AppColors.SIDEBAR);
		bottomPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

		JPanel userInfoPanel = new JPanel(new GridLayout(2, 1));
		userInfoPanel.setBackground(AppColors.SIDEBAR);

		lblName = new JLabel("Chưa đăng nhập");
		lblName.setFont(new Font("Arial", Font.BOLD, 15));
		lblName.setForeground(AppColors.TEXT);
		lblRole = new JLabel("");
		lblRole.setFont(new Font("Arial", Font.PLAIN, 13));
		lblRole.setForeground(Color.GRAY);
		userInfoPanel.add(lblName);
		userInfoPanel.add(lblRole);
		userInfoPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

		// Nút Tùy chọn (thay cho nút Đăng xuất)
		JButton btnOptions = new JButton("Tùy chọn tài khoản");
		btnOptions.setBackground(AppColors.WHITE);
		btnOptions.setForeground(AppColors.TEXT);
		btnOptions.setFont(new Font("Arial", Font.BOLD, 14));
		btnOptions.setFocusPainted(false);
		btnOptions.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppColors.BORDER),
				new EmptyBorder(10, 10, 10, 10)));
		btnOptions.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Tạo Popup Menu chứa Đổi mật khẩu & Đăng xuất
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1)); // Viền menu

		JMenuItem itemDoiMatKhau = new JMenuItem("Đổi mật khẩu");
		JMenuItem itemDangXuat = new JMenuItem("Đăng xuất");

		// Set font đậm và màu nền cho item
		Font popupFont = new Font("Arial", Font.BOLD, 14);
		itemDoiMatKhau.setFont(popupFont);
		itemDangXuat.setFont(popupFont);
		itemDoiMatKhau.setBackground(AppColors.WHITE);
		itemDangXuat.setBackground(AppColors.WHITE);

		// Thêm đệm (padding) trên dưới trái phải để mỗi mục to và rộng như nút bấm
		itemDoiMatKhau.setBorder(new EmptyBorder(12, 20, 12, 20));
		itemDangXuat.setBorder(new EmptyBorder(12, 20, 12, 20));

		popupMenu.add(itemDoiMatKhau);
		popupMenu.addSeparator(); // Đường kẻ ngang phân cách
		popupMenu.add(itemDangXuat);

		// Sự kiện click nút hiện Popup Menu (Đẩy sang phải)
		btnOptions.addActionListener(e -> {
			// 1. Ép chiều rộng popup bằng chính xác chiều rộng của nút Tùy chọn
			Dimension prefSize = popupMenu.getPreferredSize();
			popupMenu.setPreferredSize(new Dimension(btnOptions.getWidth(), prefSize.height));

			// 2. Hiển thị sang bên PHẢI nút:
			// X = btnOptions.getWidth() + 5 (cách viền nút 5px)
			// Y = canh đáy popup khớp với đáy nút
			int x = btnOptions.getWidth() + 5;
			int y = btnOptions.getHeight() - prefSize.height;
			popupMenu.show(btnOptions, x, y);
		});

		// Sự kiện Đổi mật khẩu
		itemDoiMatKhau.addActionListener(e -> {
			setActiveButton(null); // Tắt màu hồng ở Sidebar nếu đang có
			cardLayout.show(cardPanel, "Đổi mật khẩu");
		});

		// Sự kiện Đăng xuất
		itemDangXuat.addActionListener(e -> {
			int chon = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chon == JOptionPane.YES_OPTION) {
				Auth.clear();
				new LoginFrame().setVisible(true);
				this.dispose();
			}
		});

		bottomPanel.add(userInfoPanel, BorderLayout.CENTER);
		bottomPanel.add(btnOptions, BorderLayout.SOUTH);
		sidebarPanel.add(bottomPanel, BorderLayout.SOUTH);
		// Tạo trang Đổi mật khẩu ẩn để gọi từ Popup Menu
		JPanel pnlDoiMK = new JPanel(new BorderLayout());
		pnlDoiMK.setBackground(AppColors.BACKGROUND);
		JLabel lblDoiMK = new JLabel("Giao diện Đổi mật khẩu", SwingConstants.CENTER);
		lblDoiMK.setFont(new Font("Arial", Font.BOLD, 30));
		pnlDoiMK.add(lblDoiMK, BorderLayout.CENTER);
		cardPanel.add(pnlDoiMK, "Đổi mật khẩu");
		add(sidebarPanel, BorderLayout.WEST);
		add(cardPanel, BorderLayout.CENTER);

		// Gọi hàm phân quyền cuối cùng để ẩn/hiện nút và load trang chủ
		checkRole();
	}

	// --- Hàm Phân Quyền (Load giao diện theo Role) ---
	private void checkRole() {
		if (Auth.user != null) {
			// Lấy Tên Nhân Viên từ Entity NhanVien
			lblName.setText(Auth.user.getTenNV());

			// Lấy Vai Trò từ Entity TaiKhoan nằm bên trong NhanVien
			String role = Auth.user.getMaTK().getVaiTro();
			lblRole.setText(Auth.user.getChucVu()); // Hiển thị chức danh tiếng Việt (VD: "Nhân viên bán hàng")

			for (int i = 1; i < menuButtons.length; i++) {
				menuButtons[i].setVisible(false);
			}

			if (role.equalsIgnoreCase("BAN_HANG")) {
				menuButtons[4].setVisible(true);
				menuButtons[5].setVisible(true);
			} else if (role.equalsIgnoreCase("KHO")) {
				menuButtons[2].setVisible(true);
			} else if (role.equalsIgnoreCase("CHAM_SOC")) {
				menuButtons[3].setVisible(true);
				menuButtons[6].setVisible(true);
			} else if (role.equalsIgnoreCase("QUAN_LY")) {
				for (int i = 1; i < menuButtons.length; i++) {
					menuButtons[i].setVisible(true);
				}
			}
			loadHomePageCards();
			setActiveButton(menuButtons[0]);
		} else {
			// Rào lỗi nếu chạy MainFrame trực tiếp mà không qua Login
			lblName.setText("Dev Test");
			lblRole.setText("Vui lòng đăng nhập");
			loadHomePageCards();
		}
	}

	// --- Vẽ Thẻ Trang Chủ dựa trên các nút Sidebar đang hiển thị ---
	private void loadHomePageCards() {
		homePage.removeAll(); // Xóa sạch để vẽ lại

		for (int i = 1; i < MENU_ITEMS.length; i++) {
			// Chỉ vẽ thẻ nếu nút trên Sidebar tương ứng đang được hiển thị (được cấp quyền)
			if (menuButtons[i].isVisible()) {
				final int index = i;
				String item = MENU_ITEMS[i];
				JPanel card = new JPanel(new BorderLayout());
				card.setPreferredSize(new Dimension(240, 160));
				card.setBackground(AppColors.WHITE);
				card.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));

				JLabel title = new JLabel("<html><div style='text-align: center;'>" + item + "</div></html>",
						SwingConstants.CENTER);
				title.setFont(new Font("Arial", Font.BOLD, 15));
				title.setForeground(AppColors.TEXT);

				URL iconUrl = getClass().getResource("/icons/" + MENU_ICONS[i]);
				if (iconUrl != null) {
					title.setIcon(new ImageIcon(iconUrl));
					title.setVerticalTextPosition(SwingConstants.BOTTOM);
					title.setHorizontalTextPosition(SwingConstants.CENTER);
					title.setIconTextGap(15);
				}

				card.add(title, BorderLayout.CENTER);

				card.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						setActiveButton(menuButtons[index]); // Đồng bộ bật màu hồng trên Sidebar
						cardLayout.show(cardPanel, item);
					}

					public void mouseEntered(MouseEvent e) {
						card.setBorder(BorderFactory.createLineBorder(AppColors.PRIMARY, 2));
						card.setCursor(new Cursor(Cursor.HAND_CURSOR));
					}

					public void mouseExited(MouseEvent e) {
						card.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
					}
				});
				homePage.add(card);
			}
		}
		homePage.revalidate();
		homePage.repaint();
	}

	private JButton createSidebarButton(String text, String iconName) {
		JButton btn = new JButton(text);
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		btn.setAlignmentX(Component.LEFT_ALIGNMENT);
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setBackground(AppColors.SIDEBAR);
		btn.setForeground(AppColors.TEXT);
		btn.setFont(new Font("Arial", Font.BOLD, 14));
		btn.setBorder(new EmptyBorder(10, 25, 10, 20));
		btn.setIconTextGap(15);

		URL iconUrl = getClass().getResource("/icons/" + iconName);
		if (iconUrl != null) {
			btn.setIcon(new ImageIcon(iconUrl));
		}

		btn.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				if (btn != activeButton) { // Chỉ đổi màu hover nếu nút đó chưa được chọn
					btn.setBackground(AppColors.PRIMARY);
					btn.setForeground(AppColors.WHITE);
				}
				btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent e) {
				if (btn != activeButton) { // Chuột rời đi thì trả về màu gốc (nếu không phải nút đang chọn)
					btn.setBackground(AppColors.SIDEBAR);
					btn.setForeground(AppColors.TEXT);
				}
			}

			public void mouseClicked(MouseEvent e) {
				setActiveButton(btn); // Bật màu hồng cố định cho nút vừa click
				cardLayout.show(cardPanel, text);
			}
		});
		return btn;

	}

	private void setActiveButton(JButton clickedBtn) {
		// 1. Trả toàn bộ nút về màu mặc định (màu nền Sidebar, chữ Đen)
		for (JButton btn : menuButtons) {
			if (btn != null) {
				btn.setBackground(AppColors.SIDEBAR);
				btn.setForeground(AppColors.TEXT);
			}
		}
		// 2. Set màu Hồng, chữ Trắng cho riêng nút đang được click
		if (clickedBtn != null) {
			clickedBtn.setBackground(AppColors.PRIMARY);
			clickedBtn.setForeground(AppColors.WHITE);
			activeButton = clickedBtn; // Lưu lại trạng thái
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
	}
}
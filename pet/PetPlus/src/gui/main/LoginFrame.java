package gui.main;

import appColor.AppColors;
import entity.NhanVien;
import entity.TaiKhoan;
import utils.Auth;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.net.URL;

public class LoginFrame extends JFrame {

	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JButton btnLogin;

	public LoginFrame() {
		setTitle("PetPlus - Đăng nhập");
		setSize(800, 500); // Kích thước form đăng nhập
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // Căn giữa màn hình
		setResizable(false);
		setLayout(new BorderLayout());
		getContentPane().setBackground(AppColors.BACKGROUND);

		initUI();
		addEvents();
	}

	private void initUI() {
		JPanel pnlCenter = new JPanel();
		pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
		pnlCenter.setBackground(AppColors.BACKGROUND);
		pnlCenter.setBorder(new EmptyBorder(50, 200, 50, 200)); // Ép form vào giữa

		// 1. Khu vực Logo & Tên app
		JPanel pnlHeader = new JPanel();
		pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.X_AXIS)); // Xếp ngang logo và text
		pnlHeader.setBackground(AppColors.BACKGROUND);
		pnlHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Cột trái: Icon Logo
		JLabel lblIcon = new JLabel();
		try {
			URL pawIconUrl = getClass().getResource("/icons/paw.png");
			if (pawIconUrl != null) {
				ImageIcon originalIcon = new ImageIcon(pawIconUrl);
				// Resize icon to lên chút cho cân xứng với form Login
				Image scaledImage = originalIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
				lblIcon.setIcon(new ImageIcon(scaledImage));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Cột phải: Chứa 2 dòng text xếp dọc
		JPanel pnlText = new JPanel();
		pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
		pnlText.setBackground(AppColors.BACKGROUND);

		JLabel logoTitle = new JLabel("PetPlus");
		logoTitle.setFont(new Font("Arial", Font.BOLD, 42));
		logoTitle.setForeground(AppColors.PRIMARY_DARK);
		logoTitle.setAlignmentX(Component.LEFT_ALIGNMENT); // Ép sát lề trái cột text

		JLabel logoSub = new JLabel("Quản lý cửa hàng thú cưng");
		logoSub.setFont(new Font("Arial", Font.PLAIN, 16));
		logoSub.setForeground(AppColors.TEXT);
		logoSub.setAlignmentX(Component.LEFT_ALIGNMENT); // Ép sát lề trái cột text

		pnlText.add(logoTitle);
		pnlText.add(logoSub);

		// Ráp Logo và Cột Text vào Header
		pnlHeader.add(lblIcon);
		pnlHeader.add(Box.createRigidArea(new Dimension(15, 0))); // Khoảng trống giữa logo và chữ
		pnlHeader.add(pnlText);

		pnlCenter.add(pnlHeader);
		pnlCenter.add(Box.createRigidArea(new Dimension(0, 50)));

		// 2. Khu vực Form nhập liệu
		// Ô nhập Username có chữ chìm
		txtUsername = new JTextField() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (getText().isEmpty() && !isFocusOwner()) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setColor(Color.GRAY);
					g2.setFont(getFont().deriveFont(Font.ITALIC));
					// Căn giữa chữ chìm theo chiều dọc
					int y = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
					g2.drawString("Mã nhân viên (VD: NV01)", 12, y);
					g2.dispose();
				}
			}
		};
		txtUsername.setMaximumSize(new Dimension(400, 45));
		txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
		txtUsername.setBorder(new CompoundBorder(new LineBorder(AppColors.PRIMARY, 1), new EmptyBorder(5, 10, 5, 10)));

		// Ô nhập Password có chữ chìm
		txtPassword = new JPasswordField() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (new String(getPassword()).isEmpty() && !isFocusOwner()) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setColor(Color.GRAY);
					g2.setFont(getFont().deriveFont(Font.ITALIC));
					int y = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
					g2.drawString("Mật khẩu", 12, y);
					g2.dispose();
				}
			}
		};
		txtPassword.setMaximumSize(new Dimension(400, 45));
		txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
		txtPassword.setBorder(new CompoundBorder(new LineBorder(AppColors.PRIMARY, 1), new EmptyBorder(5, 10, 5, 10)));

		pnlCenter.add(txtUsername);
		pnlCenter.add(Box.createRigidArea(new Dimension(0, 20)));
		pnlCenter.add(txtPassword);
		pnlCenter.add(Box.createRigidArea(new Dimension(0, 40)));

		// 3. Nút Đăng nhập
		btnLogin = new JButton("ĐĂNG NHẬP");
		btnLogin.setMaximumSize(new Dimension(400, 50));
		btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnLogin.setBackground(AppColors.PRIMARY);
		btnLogin.setForeground(AppColors.WHITE);
		btnLogin.setFont(new Font("Arial", Font.BOLD, 18));
		btnLogin.setFocusPainted(false);
		btnLogin.setBorderPainted(false);
		btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

		pnlCenter.add(btnLogin);

		add(pnlCenter, BorderLayout.CENTER);
	}

	private void addEvents() {
		btnLogin.addActionListener(e -> xuLyDangNhap());
		txtPassword.addActionListener(e -> xuLyDangNhap()); // Bấm Enter ở ô MK cũng đăng nhập
	}

	private void xuLyDangNhap() {
		String user = txtUsername.getText().trim();
		String pass = new String(txtPassword.getPassword());

		if (user.isEmpty() || pass.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!");
			return;
		}

		// --- MOCK DATA: ĐĂNG NHẬP GIẢ LẬP ĐỂ TEST GIAO DIỆN ---
		// (Sau này code xong DAO thì đổi thành: NhanVien nvLogin =
		// taiKhoanDAO.dangNhap(user, pass); )
		NhanVien nvLogin = null;

		if (user.equals("NV01") && pass.equals("123")) {
			TaiKhoan tk = new TaiKhoan("TK01", "NV01", "123", "QUAN_LY", "Hoạt động");
			nvLogin = new NhanVien("NV01", "Trần Văn Bí", "0901234567", "079123", "HCM", "bi@gmail.com", "Quản lý", tk);

		} else if (user.equals("NV02") && pass.equals("123")) {
			TaiKhoan tk = new TaiKhoan("TK02", "NV02", "123", "KHO", "Hoạt động");
			nvLogin = new NhanVien("NV02", "Nguyễn Văn Kho", "0901234568", "079124", "HCM", "kho@gmail.com",
					"Nhân viên kho", tk);

		} else if (user.equals("NV03") && pass.equals("123")) {
			TaiKhoan tk = new TaiKhoan("TK03", "NV03", "123", "BAN_HANG", "Hoạt động");
			nvLogin = new NhanVien("NV03", "Lê Thị Bán", "0901234569", "079125", "HCM", "ban@gmail.com",
					"Nhân viên bán hàng", tk);

		} else if (user.equals("NV04") && pass.equals("123")) {
			TaiKhoan tk = new TaiKhoan("TK04", "NV04", "123", "CHAM_SOC", "Hoạt động");
			nvLogin = new NhanVien("NV04", "Phạm Chăm Sóc", "0901234570", "079126", "HCM", "chamsoc@gmail.com",
					"Nhân viên chăm sóc", tk);
		}

		// --- XỬ LÝ KẾT QUẢ ĐĂNG NHẬP ---
		if (nvLogin != null) {
			Auth.user = nvLogin;
			new MainFrame().setVisible(true);
			this.dispose();
		} else {
			// Cập nhật lại câu thông báo lỗi cho khớp với dữ liệu mới
			JOptionPane.showMessageDialog(this,
					"Sai mã nhân viên hoặc mật khẩu!\n(Mẹo: thử NV01/123, NV02/123, NV03/123, NV04/123)", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
	}
}
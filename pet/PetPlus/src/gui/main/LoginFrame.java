package gui.main;

import appColor.AppColors;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import utils.Auth;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

	private JTextField txtMaNV;
	private JPasswordField txtPassword;
	private JButton btnLogin;

	public LoginFrame() {
		setTitle("PetPlus - Đăng nhập");
		setSize(800, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
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
		// Ô nhập mã nhân viên có chữ chìm
		txtMaNV = new JTextField() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (getText().isEmpty() && !isFocusOwner()) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setColor(Color.GRAY);
					g2.setFont(getFont().deriveFont(Font.ITALIC));
					int y = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
					g2.drawString("Mã nhân viên (VD: NV001)", 12, y);
					g2.dispose();
				}
			}
		};
		txtMaNV.setMaximumSize(new Dimension(400, 45));
		txtMaNV.setFont(new Font("Arial", Font.PLAIN, 14));
		txtMaNV.setBorder(new CompoundBorder(new LineBorder(AppColors.PRIMARY, 1), new EmptyBorder(5, 10, 5, 10)));

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

		pnlCenter.add(txtMaNV);
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
		String maNV = txtMaNV.getText().trim();
		String matKhau = new String(txtPassword.getPassword());

		if (maNV.isEmpty() || matKhau.isEmpty()) {
			JOptionPane.showMessageDialog(
					this,
					"Vui lòng nhập mã nhân viên và mật khẩu!");
			return;
		}

		try {
			TaiKhoanDAO dao = new TaiKhoanDAO();
			NhanVien nv = dao.dangNhap(maNV, matKhau);

			if (nv == null) {
				JOptionPane.showMessageDialog(
						this,
						"Mã nhân viên hoặc mật khẩu không đúng, " +
								"hoặc tài khoản đã ngừng hoạt động!");
				return;
			}

			Auth.user = nv;

			MainFrame main = new MainFrame();
			main.setVisible(true);
			this.dispose();

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(
					this,
					"Lỗi kết nối hoặc truy vấn SQL Server:\n"
							+ e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
	}
}

package utils;

import entity.NhanVien;
import java.util.Locale;

public class Auth {
	public static NhanVien user = null;

	public static void clear() {
		user = null;
	}

	public static boolean isLogin() {
		return user != null;
	}

	public static String currentRole() {
		if (user == null || user.getMaTK() == null || user.getMaTK().getVaiTro() == null)
			return "";
		return user.getMaTK().getVaiTro().trim().toUpperCase(Locale.ROOT);
	}

	public static boolean hasRole(String... roles) {
		String role = currentRole();
		for (String allowedRole : roles) {
			if (allowedRole != null && allowedRole.equalsIgnoreCase(role))
				return true;
		}
		return false;
	}

	public static boolean hasFullAccess() {
		if (user == null || user.getMaTK() == null)
			return false;
		String username = user.getMaTK().getTenDangNhap();
		return (username != null && "admin".equalsIgnoreCase(username.trim()))
				|| hasRole("ADMIN", "QUAN_LY");
	}
}

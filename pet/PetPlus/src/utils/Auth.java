package utils;

import entity.NhanVien;

public class Auth {
	public static NhanVien user = null;

	public static void clear() {
		user = null;
	}

	public static boolean isLogin() {
		return user != null;
	}
}
package utils;

import java.awt.Component;
import javax.swing.JOptionPane;

public final class ThongBao {
    private ThongBao() {
    }

    public static void info(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void canhBao(Component cha, String tieuDe, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, tieuDe, JOptionPane.WARNING_MESSAGE);
    }

    public static void loi(Component cha, String tieuDe, Throwable ex) {
        JOptionPane.showMessageDialog(cha, tieuDe + ":\n" + (ex.getMessage() == null ? ex.toString() : ex.getMessage()),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }


    public static void xuLy(Component cha, String tieuDe, Exception ex) {
        if (ex instanceof IllegalArgumentException)
            canhBao(cha, tieuDe, ex.getMessage());
        else
            loi(cha, tieuDe, ex);
    }

    public static boolean xacNhan(Component cha, String noiDung) {
        return JOptionPane.showConfirmDialog(cha, noiDung, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static void daThem(Component cha, String tenDoiTuong, String ma) {
        info(cha, "Thêm " + tenDoiTuong + " thành công! Mã " + tenDoiTuong + ": " + ma);
    }

    public static void daCapNhat(Component cha, String tenDoiTuong) {
        info(cha, "Cập nhật " + tenDoiTuong + " thành công!");
    }

    public static void khongThayDoi(Component cha) {
        info(cha, "Không có dữ liệu nào được thay đổi.");
    }
}

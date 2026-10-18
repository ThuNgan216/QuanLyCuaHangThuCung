package service;

import dao.TichDiemDAO;
import entity.TichDiem;

import java.util.List;

public class TichDiemService {
    private final TichDiemDAO tichDiemDAO = new TichDiemDAO();

    public List<TichDiem> layDanhSach() {
        return tichDiemDAO.layDanhSach();
    }

    public List<TichDiem> timKiem(String tuKhoa) {
        return tichDiemDAO.timKiem(tuKhoa == null ? "" : tuKhoa.trim());
    }
}

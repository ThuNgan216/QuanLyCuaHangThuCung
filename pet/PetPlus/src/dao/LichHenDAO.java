package dao;

import database.ConnectDB;
import entity.*;
import java.sql.*;
import java.util.*;

/** Truy vấn lịch hẹn và thông tin liên quan trong SQL Server. */
public class LichHenDAO {
    private static final String SELECT = "SELECT l.maLichHen,l.thoiGianHen,l.trangThai,l.maNV,l.maKH,l.maThuCung, " +
        "nv.tenNV,kh.tenKH,tc.tenThuCung FROM LichHen l " +
        "LEFT JOIN NhanVien nv ON l.maNV=nv.maNV LEFT JOIN KhachHang kh ON l.maKH=kh.maKH " +
        "LEFT JOIN ThuCung tc ON l.maThuCung=tc.maThuCung ";

    public List<LichHen> layDanhSach() { return truyVan(SELECT + " ORDER BY l.thoiGianHen DESC", null); }
    public List<LichHen> timKiem(String key) {
        if (key == null || key.trim().isEmpty()) return layDanhSach();
        String sql = SELECT + " WHERE l.maLichHen LIKE ? OR l.trangThai LIKE ? OR l.maKH LIKE ? OR kh.tenKH LIKE ? OR l.maNV LIKE ? OR nv.tenNV LIKE ? OR l.maThuCung LIKE ? OR tc.tenThuCung LIKE ? ORDER BY l.thoiGianHen DESC";
        String k="%"+key.trim()+"%"; List<String> args=new ArrayList<>(); for(int i=0;i<8;i++) args.add(k); return truyVan(sql,args);
    }
    private List<LichHen> truyVan(String sql, List<String> args) {
        List<LichHen> ds=new ArrayList<>();
        try(Connection c=ConnectDB.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            if(args!=null) for(int i=0;i<args.size();i++) ps.setString(i+1,args.get(i));
            try(ResultSet rs=ps.executeQuery()) { while(rs.next()) { LichHen l=new LichHen(); l.setMaLichHen(rs.getString("maLichHen")); Timestamp ts=rs.getTimestamp("thoiGianHen"); l.setThoiGianHen(ts==null?null:ts.toLocalDateTime()); l.setTrangThai(rs.getString("trangThai")); l.setMaNV(new NhanVien(rs.getString("maNV"))); l.getMaNV().setTenNV(rs.getString("tenNV")); l.setMaKH(new KhachHang(rs.getString("maKH"))); l.getMaKH().setTenKH(rs.getString("tenKH")); l.setMaThuCung(new ThuCung()); l.getMaThuCung().setMaThuCung(rs.getString("maThuCung")); l.getMaThuCung().setTenThuCung(rs.getString("tenThuCung")); ds.add(l); } }
        } catch(SQLException e) { throw new IllegalStateException("Không thể tải lịch hẹn: "+e.getMessage(),e); } return ds;
    }
    public boolean tonTai(String ma) { try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement("SELECT 1 FROM LichHen WHERE maLichHen=?")){p.setString(1,ma);try(ResultSet r=p.executeQuery()){return r.next();}}catch(SQLException e){throw new IllegalStateException("Không thể kiểm tra mã lịch hẹn: "+e.getMessage(),e);} }
    public List<String> layMaNhanVien(){return layMa("SELECT maNV FROM NhanVien ORDER BY maNV");}
    public List<String> layMaKhachHang(){return layMa("SELECT maKH FROM KhachHang ORDER BY maKH");}
    public List<String> layMaThuCung(){return layMa("SELECT maThuCung FROM ThuCung ORDER BY maThuCung");}
    private List<String> layMa(String sql){List<String> a=new ArrayList<>();try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())a.add(r.getString(1));}catch(SQLException e){throw new IllegalStateException("Không thể tải dữ liệu liên quan: "+e.getMessage(),e);}return a;}
    public boolean them(LichHen l){String sql="INSERT INTO LichHen(maLichHen,thoiGianHen,trangThai,maNV,maKH,maThuCung) VALUES(?,?,?,?,?,?)";try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){gan(p,l,false);return p.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Không thể thêm lịch hẹn: "+e.getMessage(),e);}}
    public boolean capNhat(LichHen l){String sql="UPDATE LichHen SET thoiGianHen=?,trangThai=?,maNV=?,maKH=?,maThuCung=? WHERE maLichHen=?";try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setTimestamp(1,Timestamp.valueOf(l.getThoiGianHen()));p.setString(2,l.getTrangThai());p.setString(3,l.getMaNV().getMaNV());p.setString(4,l.getMaKH().getMaKH());p.setString(5,l.getMaThuCung().getMaThuCung());p.setString(6,l.getMaLichHen());return p.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Không thể cập nhật lịch hẹn: "+e.getMessage(),e);}}
    public boolean xoa(String ma){try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM LichHen WHERE maLichHen=?")){p.setString(1,ma);return p.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Không thể xóa lịch hẹn. Có thể lịch hẹn đang được dịch vụ tham chiếu: "+e.getMessage(),e);}}
    private void gan(PreparedStatement p,LichHen l,boolean update)throws SQLException{p.setString(1,l.getMaLichHen());p.setTimestamp(2,Timestamp.valueOf(l.getThoiGianHen()));p.setString(3,l.getTrangThai());p.setString(4,l.getMaNV().getMaNV());p.setString(5,l.getMaKH().getMaKH());p.setString(6,l.getMaThuCung().getMaThuCung());}
}

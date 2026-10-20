package gui.panel;

import dao.LichHenDAO;
import entity.LichHen;
import gui.dialogs.dlgLichHen;
import service.LichHenService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class pnlLichHen extends JPanel {
    private final LichHenService service=new LichHenService();
    private final JTextField txtTim=new JTextField(22);
    private final DefaultTableModel model=new DefaultTableModel(new Object[]{"Mã lịch hẹn","Thời gian","Trạng thái","Nhân viên","Mã KH","Khách hàng","Mã thú cưng","Tên thú cưng"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=new JTable(model);
    private static final DateTimeFormatter F=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public pnlLichHen(){setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));top.add(new JLabel("Tìm kiếm:"));top.add(txtTim);JButton bTim=new JButton("Tìm"),bTai=new JButton("Tải lại"),bThem=new JButton("Thêm lịch hẹn"),bSua=new JButton("Sửa"),bXoa=new JButton("Xóa");top.add(bTim);top.add(bTai);top.add(bThem);top.add(bSua);top.add(bXoa);add(top,BorderLayout.NORTH);table.setFont(new Font("Arial",Font.PLAIN,13));table.setForeground(new Color(32,44,64));table.setBackground(Color.WHITE);table.setRowHeight(36);table.setGridColor(new Color(237,217,225));table.setShowGrid(true);table.setIntercellSpacing(new Dimension(1,1));table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);table.setAutoCreateRowSorter(true);table.setFillsViewportHeight(true);table.setSelectionBackground(new Color(255,210,226));table.setSelectionForeground(new Color(32,44,64));JTableHeader th=table.getTableHeader();th.setFont(new Font("Arial",Font.BOLD,13));th.setForeground(new Color(32,44,64));th.setBackground(new Color(255,236,243));th.setReorderingAllowed(false);DefaultTableCellRenderer rr=new DefaultTableCellRenderer();rr.setBackground(Color.WHITE);rr.setForeground(new Color(32,44,64));rr.setBorder(BorderFactory.createEmptyBorder(0,8,0,8));for(int i=0;i<table.getColumnCount();i++)table.getColumnModel().getColumn(i).setCellRenderer(rr);JScrollPane sp=new JScrollPane(table);sp.getViewport().setBackground(Color.WHITE);add(sp,BorderLayout.CENTER);bTim.addActionListener(e->tim());txtTim.addActionListener(e->tim());bTai.addActionListener(e->tai());bThem.addActionListener(e->moDialog(null));bSua.addActionListener(e->{LichHen l=chonDong();if(l!=null)moDialog(l);});bXoa.addActionListener(e->xoa());tai();utils.PetPlusTheme.apply(this);}
    private void tim(){try{hienThi(service.timKiem(txtTim.getText()));}catch(RuntimeException e){baoLoi(e);}}
    private void tai(){try{txtTim.setText("");hienThi(service.layDanhSach());}catch(RuntimeException e){baoLoi(e);}}
    private void hienThi(List<LichHen> ds){model.setRowCount(0);for(LichHen l:ds)model.addRow(new Object[]{l.getMaLichHen(),l.getThoiGianHen()==null?"":l.getThoiGianHen().format(F),l.getTrangThai(),l.getMaNV()==null?"":l.getMaNV().getTenNV(),l.getMaKH()==null?"":l.getMaKH().getMaKH(),l.getMaKH()==null?"":l.getMaKH().getTenKH(),l.getMaThuCung()==null?"":l.getMaThuCung().getMaThuCung(),l.getMaThuCung()==null?"":l.getMaThuCung().getTenThuCung()});}
    private LichHen chonDong(){int row=table.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Vui lòng chọn một lịch hẹn.");return null;}String ma=model.getValueAt(table.convertRowIndexToModel(row),0).toString();try{for(LichHen l:service.layDanhSach())if(l.getMaLichHen().equals(ma))return l;}catch(RuntimeException e){baoLoi(e);}return null;}
    private void moDialog(LichHen l){Window w=SwingUtilities.getWindowAncestor(this);dlgLichHen d=new dlgLichHen(w,l);d.setVisible(true);if(d.isSaved())tai();}
    private void xoa(){LichHen l=chonDong();if(l==null)return;if(JOptionPane.showConfirmDialog(this,"Bạn có chắc muốn xóa lịch hẹn "+l.getMaLichHen()+"?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try{service.xoa(l.getMaLichHen());tai();}catch(RuntimeException e){baoLoi(e);}}
    private void baoLoi(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}
}

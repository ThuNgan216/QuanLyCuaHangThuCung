package gui.dialogs;

import dao.LichHenDAO;
import entity.*;
import service.LichHenService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;

public class dlgLichHen extends JDialog {
    private final JTextField txtMa=new JTextField(18), txtThoiGian=new JTextField(18);
    private final JButton btnChonThoiGian = new JButton("Chọn ngày, giờ...");
    private final JComboBox<TrangThaiLichHen> cboTrangThai=new JComboBox<>(TrangThaiLichHen.values());
    private final JComboBox<String> cboNV=new JComboBox<>(), cboKH=new JComboBox<>(), cboThuCung=new JComboBox<>();
    private final LichHenService service=new LichHenService();
    private final LichHen duLieu; private boolean saved=false;
    private static final DateTimeFormatter F=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public dlgLichHen(Window owner,LichHen lichHen){super(owner,lichHen==null?"Thêm lịch hẹn":"Cập nhật lịch hẹn",ModalityType.APPLICATION_MODAL);this.duLieu=lichHen; khoiTao();}
    private void khoiTao(){setLayout(new BorderLayout(10,10));JPanel form=new JPanel(new GridBagLayout());form.setBorder(BorderFactory.createEmptyBorder(15,18,8,18));GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(5,5,5,5);g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;int y=0;
        themDong(form,g,y++,"Mã lịch hẹn:",txtMa);JPanel pnlThoiGian = new JPanel(new BorderLayout(6, 0));
        txtThoiGian.setEditable(false);
        pnlThoiGian.add(txtThoiGian, BorderLayout.CENTER);
        pnlThoiGian.add(btnChonThoiGian, BorderLayout.EAST);
        themDong(form,g,y++,"Thời gian hẹn:",pnlThoiGian);
        btnChonThoiGian.addActionListener(e -> moChonNgayGio());themDong(form,g,y++,"Trạng thái:",cboTrangThai);themDong(form,g,y++,"Mã nhân viên:",cboNV);themDong(form,g,y++,"Mã khách hàng:",cboKH);themDong(form,g,y++,"Mã thú cưng:",cboThuCung);
        try{for(String s:service.layMaNhanVien())cboNV.addItem(s);for(String s:service.layMaKhachHang())cboKH.addItem(s);for(String s:service.layMaThuCung())cboThuCung.addItem(s);}catch(RuntimeException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi tải dữ liệu",JOptionPane.ERROR_MESSAGE);}
        if(duLieu!=null){txtMa.setText(duLieu.getMaLichHen());txtMa.setEditable(false);if(duLieu.getThoiGianHen()!=null)txtThoiGian.setText(duLieu.getThoiGianHen().format(F));cboTrangThai.setSelectedItem(TrangThaiLichHen.tuTen(duLieu.getTrangThai()));chon(cboNV,duLieu.getMaNV()==null?null:duLieu.getMaNV().getMaNV());chon(cboKH,duLieu.getMaKH()==null?null:duLieu.getMaKH().getMaKH());chon(cboThuCung,duLieu.getMaThuCung()==null?null:duLieu.getMaThuCung().getMaThuCung());}else{txtThoiGian.setText(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).format(F));}
        JButton btnLuu=new JButton("Lưu"),btnHuy=new JButton("Hủy");JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));buttons.add(btnLuu);buttons.add(btnHuy);btnLuu.addActionListener(e->luu());btnHuy.addActionListener(e->dispose());add(form,BorderLayout.CENTER);add(buttons,BorderLayout.SOUTH);utils.PetPlusTheme.apply(getContentPane());pack();setLocationRelativeTo(getOwner());}
    private void themDong(JPanel p,GridBagConstraints g,int y,String label,JComponent c){g.gridx=0;g.gridy=y;g.weightx=0;p.add(new JLabel(label),g);g.gridx=1;g.weightx=1;p.add(c,g);}
    private void chon(JComboBox<String> c,String v){if(v!=null)c.setSelectedItem(v);}
    /** Mở lịch chọn ngày và giờ; người dùng không cần gõ thời gian bằng tay. */
    private void moChonNgayGio() {
        LocalDateTime hienTai;
        try {
            hienTai = txtThoiGian.getText().trim().isEmpty()
                    ? LocalDateTime.now().plusDays(1).withSecond(0).withNano(0)
                    : LocalDateTime.parse(txtThoiGian.getText().trim(), F);
        } catch (Exception ex) {
            hienTai = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        }
        final LocalDateTime banDau = hienTai;
        JDialog dlg = new JDialog(this, "Chọn ngày và giờ hẹn", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(8, 8));
        JPanel calendar = new JPanel(new BorderLayout(5, 5));
        calendar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JPanel header = new JPanel(new BorderLayout(8, 0));
        JButton prev = new JButton("◀");
        JButton next = new JButton("▶");
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 15f));
        header.add(prev, BorderLayout.WEST); header.add(monthLabel, BorderLayout.CENTER); header.add(next, BorderLayout.EAST);
        calendar.add(header, BorderLayout.NORTH);
        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 3, 3));
        calendar.add(daysPanel, BorderLayout.CENTER);
        LocalDate[] selectedDate = { banDau.toLocalDate() };
        YearMonth[] shownMonth = { YearMonth.from(selectedDate[0]) };
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        timePanel.add(new JLabel("Giờ:"));
        JSpinner hour = new JSpinner(new SpinnerNumberModel(banDau.getHour(), 0, 23, 1));
        hour.setEditor(new JSpinner.NumberEditor(hour, "00"));
        timePanel.add(hour);
        timePanel.add(new JLabel("Phút:"));
        JSpinner minute = new JSpinner(new SpinnerNumberModel(banDau.getMinute(), 0, 59, 1));
        minute.setEditor(new JSpinner.NumberEditor(minute, "00"));
        timePanel.add(minute);
        JButton ok = new JButton("Chọn");
        JButton cancel = new JButton("Hủy");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(ok); actions.add(cancel);
        JPanel south = new JPanel(new BorderLayout());
        south.add(timePanel, BorderLayout.CENTER); south.add(actions, BorderLayout.SOUTH);
        dlg.add(calendar, BorderLayout.CENTER); dlg.add(south, BorderLayout.SOUTH);

        Runnable[] renderCalendar = new Runnable[1];
        renderCalendar[0] = new Runnable() {
            @Override public void run() {
                monthLabel.setText(shownMonth[0].getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("vi", "VN")) + " " + shownMonth[0].getYear());
                daysPanel.removeAll();
                String[] weekdays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                for (String day : weekdays) daysPanel.add(new JLabel(day, SwingConstants.CENTER));
                LocalDate first = shownMonth[0].atDay(1);
                int offset = first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
                for (int i = 0; i < offset; i++) daysPanel.add(new JLabel(""));
                for (int d = 1; d <= shownMonth[0].lengthOfMonth(); d++) {
                    final LocalDate date = shownMonth[0].atDay(d);
                    JButton b = new JButton(Integer.toString(d));
                    b.setMargin(new Insets(4, 4, 4, 4));
                    if (date.equals(selectedDate[0])) {
                        b.setBackground(utils.PetPlusTheme.PRIMARY);
                        b.setForeground(Color.WHITE);
                    }
                    b.addActionListener(e -> { selectedDate[0] = date; renderCalendar[0].run(); });
                    daysPanel.add(b);
                }
                daysPanel.revalidate(); daysPanel.repaint();
            }
        };
        prev.addActionListener(e -> { shownMonth[0] = shownMonth[0].minusMonths(1); renderCalendar[0].run(); });
        next.addActionListener(e -> { shownMonth[0] = shownMonth[0].plusMonths(1); renderCalendar[0].run(); });
        ok.addActionListener(e -> {
            LocalDateTime value = selectedDate[0].atTime((Integer) hour.getValue(), (Integer) minute.getValue());
            txtThoiGian.setText(value.format(F));
            dlg.dispose();
        });
        cancel.addActionListener(e -> dlg.dispose());
        utils.PetPlusTheme.apply(dlg.getContentPane());
        renderCalendar[0].run();
        dlg.pack(); dlg.setMinimumSize(new Dimension(330, 330)); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void luu(){try{if(cboNV.getSelectedItem()==null||cboKH.getSelectedItem()==null||cboThuCung.getSelectedItem()==null)throw new IllegalArgumentException("Cần có ít nhất một nhân viên, khách hàng và thú cưng trong cơ sở dữ liệu.");if (txtThoiGian.getText().trim().isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ngày và giờ hẹn.");LocalDateTime time=LocalDateTime.parse(txtThoiGian.getText().trim(),F);LichHen l=new LichHen();l.setMaLichHen(txtMa.getText().trim());l.setThoiGianHen(time);l.setTrangThai(((TrangThaiLichHen)cboTrangThai.getSelectedItem()).getTenHienThi());l.setMaNV(new NhanVien(cboNV.getSelectedItem().toString()));l.setMaKH(new KhachHang(cboKH.getSelectedItem().toString()));l.setMaThuCung(new ThuCung());l.getMaThuCung().setMaThuCung(cboThuCung.getSelectedItem().toString());if(duLieu==null)service.them(l);else service.capNhat(l);saved=true;dispose();}catch(Exception ex){JOptionPane.showMessageDialog(this,"Không thể lưu lịch hẹn: "+ex.getMessage()+"\nVui lòng chọn thời gian bằng nút Chọn ngày, giờ.","Lỗi dữ liệu",JOptionPane.ERROR_MESSAGE);}}
    public boolean isSaved(){return saved;}
}

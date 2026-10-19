package gui.panel;

import dao.LichLamViecDAO;
import dao.LichLamViecDAO.LichItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class pnlXemLichLamViec extends JPanel {
    private static final Color PRIMARY = new Color(245, 70, 113);
    private static final Color DARK = new Color(220, 27, 86);
    private static final Color BG = new Color(255, 247, 250);
    private static final Color LIGHT = new Color(255, 236, 243);
    private static final Color CARD = new Color(249, 176, 200);
    private static final Color TEXT = new Color(32, 44, 64);
    private static final Color MUTED = new Color(118, 126, 140);
    private static final Color BORDER = new Color(237, 217, 225);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] THU = {
        "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"
    };

    private final LichLamViecDAO dao = new LichLamViecDAO();
    private final JLabel lblKhoangNgay = new JLabel();
    private final JLabel lblTong = new JLabel("0 lịch đã xếp");
    private final BangTuan lichTuan = new BangTuan();
    private LocalDate thuHai = LocalDate.now().with(DayOfWeek.MONDAY);
    private int phienTai = 0;

    public pnlXemLichLamViec() {
        setLayout(new BorderLayout(0, 18));
        setBackground(BG);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel top = new JPanel(new BorderLayout(0, 15));
        top.setOpaque(false);
        JLabel title = nhan("Xem lịch làm việc", 26, DARK, true);
        top.add(title, BorderLayout.NORTH);
        JPanel line = new JPanel();
        line.setPreferredSize(new Dimension(0, 2));
        line.setBackground(PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 5));
        controls.setOpaque(false);
        JLabel label = nhan("Khoảng thời gian:", 13, TEXT, false);
        lblKhoangNgay.setFont(new Font("Arial", Font.BOLD, 13));
        lblKhoangNgay.setForeground(TEXT);
        JButton prev = nut("← Tuần trước", false);
        JButton today = nut("Tuần hiện tại", false);
        JButton next = nut("Tuần sau →", false);
        prev.addActionListener(e -> { thuHai = thuHai.minusWeeks(1); taiLich(); });
        today.addActionListener(e -> {
            thuHai = LocalDate.now().with(DayOfWeek.MONDAY); taiLich();
        });
        next.addActionListener(e -> { thuHai = thuHai.plusWeeks(1); taiLich(); });
       
        controls.add(label);
        controls.add(lblKhoangNgay);
        controls.add(prev);
        controls.add(today);
        controls.add(next);
     

        JPanel tools = new JPanel(new BorderLayout(0, 12));
        tools.setOpaque(false);
        tools.add(line, BorderLayout.NORTH);
        tools.add(controls, BorderLayout.CENTER);
        top.add(tools, BorderLayout.CENTER);
        lblTong.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTong.setForeground(MUTED);
        top.add(lblTong, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        lichTuan.setBackground(BORDER);
        lichTuan.setBorder(BorderFactory.createLineBorder(BORDER));
        JScrollPane scroll = new JScrollPane(lichTuan,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);
        add(scroll, BorderLayout.CENTER);
        taiLich();
    }

    private static JLabel nhan(String text, int size, Color color, boolean bold) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        result.setForeground(color);
        return result;
    }

    private static JButton nut(String text, boolean main) {
        JButton b = new JButton(text);
        b.setBackground(main ? PRIMARY : Color.WHITE);
        b.setForeground(main ? Color.WHITE : TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(main ? PRIMARY : BORDER),
                new EmptyBorder(10, 12, 10, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

   
    public void chuyenDenTuan(LocalDate ngay) {
        if (ngay != null) {
            thuHai = ngay.with(DayOfWeek.MONDAY);
            taiLich();
        }
    }

    public void taiLich() {
        final LocalDate start = thuHai;
        final LocalDate end = start.plusDays(6);
        lblKhoangNgay.setText(start.format(DATE) + " đến " + end.format(DATE));
        final int request = ++phienTai;
        new SwingWorker<List<LichItem>, Void>() {
            @Override protected List<LichItem> doInBackground() throws Exception {
                return dao.layLich(start, end);
            }
            @Override protected void done() {
                if (request != phienTai) return;
                try {
                    List<LichItem> ds = get();
                    hienThiTuan(start, ds);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable root = ex.getCause();
                    JOptionPane.showMessageDialog(pnlXemLichLamViec.this,
                            root == null ? ex.getMessage() : root.getMessage(),
                            "Không tải được lịch từ SQL Server", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

   
    private static final class BangTuan extends JPanel implements Scrollable {
        private int chieuCao = 510;

        BangTuan() {
            super(new GridLayout(1, 7, 4, 0));
        }

        void datChieuCao(int height) {
            chieuCao = Math.max(450, height);
            revalidate();
        }

        @Override public Dimension getPreferredSize() {
        
            return new Dimension(1000, chieuCao);
        }
        @Override public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(1000, 510);
        }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect,
                int orientation, int direction) { return 22; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect,
                int orientation, int direction) { return Math.max(100, visibleRect.height - 60); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private void hienThiTuan(LocalDate monday, List<LichItem> ds) {
        Map<LocalDate, List<LichItem>> theoNgay = new HashMap<LocalDate, List<LichItem>>();
        int maxTrongNgay = 0;
        for (LichItem item : ds) {
            theoNgay.computeIfAbsent(item.ngayLamViec, key -> new ArrayList<LichItem>())
                    .add(item);
            maxTrongNgay = Math.max(maxTrongNgay, theoNgay.get(item.ngayLamViec).size());
        }
        lichTuan.removeAll();
        for (int i = 0; i < 7; i++) {
            LocalDate ngay = monday.plusDays(i);
            // BorderLayout giu header cao 54px, rong vua dung 1/7 khung.
            JPanel cot = new JPanel(new BorderLayout());
            cot.setBackground(Color.WHITE);
            cot.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

            JLabel header = nhan(THU[i] + " " + ngay.format(DateTimeFormatter.ofPattern("dd/MM")),
                    12, TEXT, true);
            header.setOpaque(true);
            header.setBackground(LIGHT);
            header.setHorizontalAlignment(SwingConstants.CENTER);
            header.setPreferredSize(new Dimension(0, 54));
            cot.add(header, BorderLayout.NORTH);

            JPanel danhSach = new JPanel();
            danhSach.setLayout(new BoxLayout(danhSach, BoxLayout.Y_AXIS));
            danhSach.setBackground(Color.WHITE);
            danhSach.setBorder(new EmptyBorder(10, 4, 6, 4));
            List<LichItem> dsNgay = theoNgay.get(ngay);
            if (dsNgay == null || dsNgay.isEmpty()) {
                JLabel trong = nhan("Chưa có lịch", 11, MUTED, false);
                trong.setAlignmentX(Component.CENTER_ALIGNMENT);
                danhSach.add(trong);
            } else {
                for (LichItem item : dsNgay) {
                    JPanel holder = new JPanel(new BorderLayout());
                    holder.setOpaque(false);
                    holder.setBorder(new EmptyBorder(0, 3, 9, 3));
                    // Quan trong: holder chi gioi han CHIEU CAO, KHONG ep rong cot.
                    holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
                    holder.setPreferredSize(new Dimension(0, 114));

                    JPanel card = new JPanel();
                    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                    card.setBackground(CARD);
                    card.setBorder(new EmptyBorder(10, 8, 10, 6));

                    JLabel gio = nhan(item.ca.getGioBatDau().format(TIME) + " - "
                            + item.ca.getGioKetThuc().format(TIME), 12, TEXT, true);
                    JLabel tenCa = nhan(item.ca.getTenCa(), 12, TEXT, false);
                
                    JTextArea tenNV = new JTextArea(item.tenNV);
                    tenNV.setEditable(false);
                    tenNV.setFocusable(false);
                    tenNV.setOpaque(false);
                    tenNV.setLineWrap(true);
                    tenNV.setWrapStyleWord(true);
                    tenNV.setRows(2);
                    tenNV.setFont(new Font("Arial", Font.BOLD, 12));
                    tenNV.setForeground(DARK);
                    tenNV.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                    tenNV.setPreferredSize(new Dimension(80, 34));
                    String tooltip = "Nhân viên: " + item.tenNV + " (" + item.maNV + ") | "
                            + item.ca.getTenCa() + " | " + item.ca.getGioBatDau().format(TIME)
                            + " - " + item.ca.getGioKetThuc().format(TIME);
                    tenNV.setToolTipText(tooltip);
                    card.setToolTipText(tooltip);
                    gio.setAlignmentX(Component.LEFT_ALIGNMENT);
                    tenCa.setAlignmentX(Component.LEFT_ALIGNMENT);
                    tenNV.setAlignmentX(Component.LEFT_ALIGNMENT);
                    card.add(gio);
                    card.add(Box.createVerticalStrut(5));
                    card.add(tenCa);
                    card.add(Box.createVerticalStrut(6));
                    card.add(tenNV);
                    holder.add(card, BorderLayout.CENTER);
                    danhSach.add(holder);
                }
            }
            cot.add(danhSach, BorderLayout.CENTER);
            lichTuan.add(cot);
        }
        lblTong.setText("Tổng số lịch trong tuần: " + ds.size());
        lichTuan.datChieuCao(54 + 25 + maxTrongNgay * 116 + 20);
        lichTuan.revalidate();
        lichTuan.repaint();
    }
}

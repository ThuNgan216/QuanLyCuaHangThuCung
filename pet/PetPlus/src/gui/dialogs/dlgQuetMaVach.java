package gui.dialogs;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ReaderException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class dlgQuetMaVach extends JDialog {

    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private final JPanel cameraContainer;
    private final JButton btnBatDau;
    private final JButton btnDung;
    private final JLabel lblKetQua;
    private final Consumer<String> onMaVachQuet;
    private Timer scanTimer;
    private boolean dangMo = false;
    private boolean dangQuet = false;
    private long lanQuet = 0;
    private String maVachChoXacNhan;
    private int soKhungLienTiep;

    public dlgQuetMaVach(Frame parent) {
        this(parent, null);
    }

    public dlgQuetMaVach(Frame parent, Consumer<String> onMaVachQuet) {
        super(parent, "Quét mã vạch - PetPlus", false);
        this.onMaVachQuet = onMaVachQuet;

        setSize(700, 550);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        cameraContainer = new JPanel(new BorderLayout());
        cameraContainer.add(
                new JLabel("Nhấn Bắt đầu để mở camera", SwingConstants.CENTER),
                BorderLayout.CENTER);
        add(cameraContainer, BorderLayout.CENTER);

        lblKetQua = new JLabel("Chưa nhận diện mã vạch", SwingConstants.CENTER);
        lblKetQua.setFont(new Font("Times New Roman", Font.BOLD, 16));
        lblKetQua.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnBatDau = new JButton("Bắt đầu");
        btnDung = new JButton("Dừng");
        JButton btnDong = new JButton("Đóng");
        btnDung.setEnabled(false);
        buttonPanel.add(btnBatDau);
        buttonPanel.add(btnDung);
        buttonPanel.add(btnDong);

        JPanel footer = new JPanel(new BorderLayout());
        footer.add(lblKetQua, BorderLayout.NORTH);
        footer.add(buttonPanel, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);

        btnBatDau.addActionListener(e -> batCamera());
        btnDung.addActionListener(e -> dungCamera());
        btnDong.addActionListener(e -> dispose());
    }

    // Bật webcam và hiển thị hình ảnh
    private void batCamera() {
        if (dangMo && webcam != null && webcam.isOpen()) {
            btnBatDau.setEnabled(false);
            lblKetQua.setText("Đưa mã vạch tiếp theo vào giữa camera...");
            batQuetTuDong();
            return;
        }
        if (dangMo)
            return;
        dangMo = true;
        btnBatDau.setEnabled(false);
        btnBatDau.setText("Đang mở...");
        lblKetQua.setText("Đang mở webcam...");

        new SwingWorker<Webcam, Void>() {
            @Override
            protected Webcam doInBackground() {
                Webcam camera = Webcam.getDefault();
                if (camera == null) {
                    throw new IllegalStateException("Không tìm thấy webcam!");
                }
                // Chọn độ phân giải cao nhất mà webcam hỗ trợ
                Dimension bestSize = camera.getViewSizes()[0];

                for (Dimension size : camera.getViewSizes()) {
                    if (size.width * size.height > bestSize.width * bestSize.height) {
                        bestSize = size;
                    }
                }

                camera.setViewSize(bestSize);
                System.out.println("Độ phân giải webcam: " + bestSize.width + "x" + bestSize.height);

                camera.open();
                return camera;
            }

            @Override
            protected void done() {
                try {
                    Webcam camera = get();
                    if (!isDisplayable() || !dangMo) {
                        new Thread(camera::close, "close-webcam").start();
                        return;
                    }

                    webcam = camera;
                    webcamPanel = new WebcamPanel(webcam, false);
                    webcamPanel.setFPSDisplayed(true);
                    webcamPanel.setMirrored(false);

                    cameraContainer.removeAll();
                    cameraContainer.add(webcamPanel, BorderLayout.CENTER);
                    cameraContainer.revalidate();
                    cameraContainer.repaint();

                    webcamPanel.start();
                    btnDung.setEnabled(true);
                    btnBatDau.setText("Đang quét...");
                    lblKetQua.setText("Đưa mã vạch vào giữa camera...");
                    batQuetTuDong();
                } catch (Exception ex) {
                    dangMo = false;
                    btnBatDau.setEnabled(true);
                    btnBatDau.setText("Bắt đầu");
                    lblKetQua.setText("Không mở được webcam");
                    JOptionPane.showMessageDialog(dlgQuetMaVach.this,
                            "Lỗi mở webcam: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // Đọc mã liên tục mà không khóa giao diện Swing
    private void batQuetTuDong() {
        lanQuet++;
        maVachChoXacNhan = null;
        soKhungLienTiep = 0;
        scanTimer = new Timer(350, e -> {
            if (!dangMo || dangQuet || webcam == null || !webcam.isOpen())
                return;
            dangQuet = true;
            final Webcam camera = webcam;
            final long phien = lanQuet;

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    try {
                        BufferedImage image = camera.getImage();
                        if (image == null)
                            return null;

                        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
                        hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                                BarcodeFormat.CODE_128,
                                BarcodeFormat.CODE_39,
                                BarcodeFormat.EAN_13,
                                BarcodeFormat.EAN_8,
                                BarcodeFormat.UPC_A,
                                BarcodeFormat.UPC_E,
                                BarcodeFormat.ITF,
                                BarcodeFormat.QR_CODE));
                        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                        hints.put(DecodeHintType.ALSO_INVERTED, Boolean.TRUE);

                        return docGiaiMaVach(image, hints);
                    } catch (Exception ex) {
                        System.err.println("Lỗi đọc mã vạch: " + ex.getMessage());
                        return null;
                    }
                }

                @Override
                protected void done() {
                    dangQuet = false;
                    if (!dangMo || phien != lanQuet || !isDisplayable())
                        return;
                    try {
                        String maVach = get();
                        if (maVach == null || maVach.isBlank()) {
                            maVachChoXacNhan = null;
                            soKhungLienTiep = 0;
                            return;
                        }

                        if (maVach.equals(maVachChoXacNhan)) {
                            soKhungLienTiep++;
                        } else {
                            maVachChoXacNhan = maVach;
                            soKhungLienTiep = 1;
                        }
                        if (soKhungLienTiep < 2) {
                            lblKetQua.setText("Đã thấy mã " + maVach
                                    + " • Giữ mã ở giữa camera để xác nhận (1/2).");
                            return;
                        }

                        lblKetQua.setText(onMaVachQuet == null
                                ? "Mã vạch: " + maVach
                                : "Đã quét: " + maVach + " • Nhấn Quét tiếp để quét mã khác.");
                        System.out.println("Mã vạch được xác nhận: " + maVach);
                        if (scanTimer != null) scanTimer.stop();
                        btnBatDau.setText("Quét tiếp");
                        btnBatDau.setEnabled(true);
                        maVachChoXacNhan = null;
                        soKhungLienTiep = 0;
                        if (onMaVachQuet != null) onMaVachQuet.accept(maVach.trim());
                    } catch (Exception ex) {
                        System.err.println("Lỗi xử lý mã vạch: " + ex.getMessage());
                    }
                }
            }.execute();
        });
        scanTimer.start();
    }

    /** Thử khung hình gốc trước, rồi tăng tương phản/crop/phóng to khi mã khó đọc. */
    private static String docGiaiMaVach(BufferedImage image, Map<DecodeHintType, Object> hints) {
        String ma = thuGiaiMa(image, hints);
        if (ma != null) return ma;

        // Mã có thể gần sát mép khung hình; tăng tương phản toàn ảnh trước để không cắt mất quiet zone.
        BufferedImage roNetToanAnh = tangTuongPhan(image);
        ma = thuGiaiMa(roNetToanAnh, hints);
        if (ma != null) return ma;

        // Chỉ bỏ một viền nhỏ quanh ảnh, giữ lại gần như toàn bộ chiều dài mã.
        BufferedImage crop = catGiua(image, 0.98, 0.94);
        BufferedImage roNet = tangTuongPhan(crop);
        BufferedImage phongTo = phongTo(roNet, 1.5);
        ma = thuGiaiMa(phongTo, hints);
        if (ma != null) return ma;

        // Hỗ trợ mã bị xoay dọc trước camera điện thoại.
        ma = thuGiaiMa(xoay(phongTo, true), hints);
        if (ma != null) return ma;
        return thuGiaiMa(xoay(phongTo, false), hints);
    }

    private static String thuGiaiMa(BufferedImage image, Map<DecodeHintType, Object> hints) {
        String ma = thuGiaiMa(image, hints, true);
        return ma != null ? ma : thuGiaiMa(image, hints, false);
    }

    private static String thuGiaiMa(BufferedImage image, Map<DecodeHintType, Object> hints,
            boolean dungHybrid) {
        try {
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(dungHybrid
                    ? new HybridBinarizer(source)
                    : new GlobalHistogramBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            reader.setHints(hints);
            Result result = reader.decodeWithState(bitmap);
            return namTrongVungQuet(result, image) ? result.getText() : null;
        } catch (ReaderException | IllegalArgumentException ex) {
            return null;
        }
    }

    /** Bỏ mã nhỏ hoặc nằm sát rìa để tránh bắt nhầm mã trên đồ vật phía sau. */
    private static boolean namTrongVungQuet(Result result, BufferedImage image) {
        ResultPoint[] points = result.getResultPoints();
        if (points == null || points.length < 2) return false;

        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        for (ResultPoint point : points) {
            if (point == null) continue;
            minX = Math.min(minX, point.getX());
            maxX = Math.max(maxX, point.getX());
            minY = Math.min(minY, point.getY());
            maxY = Math.max(maxY, point.getY());
        }
        if (minX == Float.MAX_VALUE || minY == Float.MAX_VALUE) return false;

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        if (centerX < image.getWidth() * 0.20 || centerX > image.getWidth() * 0.80
                || centerY < image.getHeight() * 0.15 || centerY > image.getHeight() * 0.85) {
            return false;
        }

        double spanX = (maxX - minX) / image.getWidth();
        double spanY = (maxY - minY) / image.getHeight();
        return Math.max(spanX, spanY) >= 0.18;
    }

    private static BufferedImage catGiua(BufferedImage image, double tiLeRong, double tiLeCao) {
        int width = Math.max(1, (int) Math.round(image.getWidth() * tiLeRong));
        int height = Math.max(1, (int) Math.round(image.getHeight() * tiLeCao));
        int x = (image.getWidth() - width) / 2;
        int y = (image.getHeight() - height) / 2;
        return image.getSubimage(x, y, width, height);
    }

    /** Kéo giãn tương phản theo percentile để giảm ảnh hưởng của lóe sáng trên màn hình. */
    private static BufferedImage tangTuongPhan(BufferedImage image) {
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = gray.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        WritableRaster raster = gray.getRaster();
        int[] histogram = new int[256];
        long total = (long) gray.getWidth() * gray.getHeight();
        for (int y = 0; y < gray.getHeight(); y++) {
            for (int x = 0; x < gray.getWidth(); x++) {
                histogram[raster.getSample(x, y, 0)]++;
            }
        }

        int low = percentile(histogram, total * 2 / 100);
        int high = percentile(histogram, total * 98 / 100);
        if (high <= low) return gray;

        for (int y = 0; y < gray.getHeight(); y++) {
            for (int x = 0; x < gray.getWidth(); x++) {
                int value = raster.getSample(x, y, 0);
                int adjusted = Math.max(0, Math.min(255,
                        (value - low) * 255 / (high - low)));
                raster.setSample(x, y, 0, adjusted);
            }
        }
        return gray;
    }

    private static int percentile(int[] histogram, long target) {
        long count = 0;
        for (int i = 0; i < histogram.length; i++) {
            count += histogram[i];
            if (count >= target) return i;
        }
        return histogram.length - 1;
    }

    private static BufferedImage phongTo(BufferedImage image, double tiLe) {
        int width = Math.max(1, (int) Math.round(image.getWidth() * tiLe));
        int height = Math.max(1, (int) Math.round(image.getHeight() * tiLe));
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        return scaled;
    }

    private static BufferedImage xoay(BufferedImage image, boolean clockwise) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage rotated = new BufferedImage(height, width, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = rotated.createGraphics();
        if (clockwise) {
            graphics.translate(height, 0);
            graphics.rotate(Math.PI / 2);
        } else {
            graphics.translate(0, width);
            graphics.rotate(-Math.PI / 2);
        }
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return rotated;
    }

    // Dừng camera và giải phóng thiết bị
    private void dungCamera() {
        if (!dangMo)
            return;
        dangMo = false;
        lanQuet++;
        if (scanTimer != null) {
            scanTimer.stop();
            scanTimer = null;
        }
        btnDung.setEnabled(false);
        btnBatDau.setEnabled(true);
        btnBatDau.setText("Bắt đầu");

        WebcamPanel panel = webcamPanel;
        Webcam camera = webcam;
        webcamPanel = null;
        webcam = null;

        cameraContainer.removeAll();
        cameraContainer.add(new JLabel("Camera đã dừng", SwingConstants.CENTER), BorderLayout.CENTER);
        cameraContainer.revalidate();
        cameraContainer.repaint();

        new Thread(() -> {
            try {
                if (panel != null)
                    panel.stop();
                if (camera != null && camera.isOpen())
                    camera.close();
            } catch (Exception ex) {
                System.err.println("Lỗi đóng camera: " + ex.getMessage());
            }
        }, "stop-webcam").start();
    }

    @Override
    public void dispose() {
        dungCamera();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            dlgQuetMaVach dialog = new dlgQuetMaVach(null);
            dialog.setVisible(true);
        });
    }
}

package gui.dialogs;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class dlgQuetMaVach extends JDialog {

    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private final JPanel cameraContainer;
    private final JButton btnBatDau;
    private final JButton btnDung;
    private final JLabel lblKetQua;
    private Timer scanTimer;
    private boolean dangMo = false;
    private boolean dangQuet = false;
    private long lanQuet = 0;

    public dlgQuetMaVach(Frame parent) {
        super(parent, "Quét mã vạch - PetPlus", false);

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
        if (dangMo)
            return;
        dangMo = true;
        btnBatDau.setEnabled(false);
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
                    lblKetQua.setText("Đưa mã vạch vào giữa camera...");
                    batQuetTuDong();
                } catch (Exception ex) {
                    dangMo = false;
                    btnBatDau.setEnabled(true);
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
                                BarcodeFormat.EAN_13,
                                BarcodeFormat.EAN_8,
                                BarcodeFormat.UPC_A,
                                BarcodeFormat.QR_CODE));
                        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

                        MultiFormatReader reader = new MultiFormatReader();
                        reader.setHints(hints);
                        BinaryBitmap bitmap = new BinaryBitmap(
                                new HybridBinarizer(new BufferedImageLuminanceSource(image)));
                        Result result = reader.decodeWithState(bitmap);
                        return result.getText();
                    } catch (NotFoundException ex) {
                        return null;
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
                        if (maVach != null && !maVach.isBlank()) {
                            lblKetQua.setText("Mã vạch: " + maVach);
                            System.out.println("Mã vạch đọc được: " + maVach);
                            scanTimer.stop();
                        }
                    } catch (Exception ex) {
                        System.err.println("Lỗi xử lý mã vạch: " + ex.getMessage());
                    }
                }
            }.execute();
        });
        scanTimer.start();
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

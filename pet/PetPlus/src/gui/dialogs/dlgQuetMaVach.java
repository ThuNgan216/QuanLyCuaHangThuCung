package gui.main.dialogs;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

import javax.swing.*;
import java.awt.*;

public class dlgQuetMaVach extends JDialog {

    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private final JPanel cameraContainer;
    private final JButton btnBatDau;
    private final JButton btnDung;
    private boolean dangMo = false;

    public dlgQuetMaVach(Frame parent) {
        super(parent, "Quét mã vạch - PetPlus", false);

        setSize(700, 550);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        cameraContainer = new JPanel(new BorderLayout());
        cameraContainer.add(
            new JLabel("Nhấn Bắt đầu để mở camera",
                SwingConstants.CENTER),
            BorderLayout.CENTER
        );

        add(cameraContainer, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        btnBatDau = new JButton("Bắt đầu");
        btnDung = new JButton("Dừng");
        JButton btnDong = new JButton("Đóng");

        btnDung.setEnabled(false);

        buttonPanel.add(btnBatDau);
        buttonPanel.add(btnDung);
        buttonPanel.add(btnDong);

        add(buttonPanel, BorderLayout.SOUTH);

        btnBatDau.addActionListener(e -> batCamera());
        btnDung.addActionListener(e -> dungCamera());
        btnDong.addActionListener(e -> dispose());
    }

    private void batCamera() {
        if (dangMo) return;

        dangMo = true;
        btnBatDau.setEnabled(false);

        new SwingWorker<Webcam, Void>() {

            @Override
            protected Webcam doInBackground() {
                Webcam camera = Webcam.getDefault();

                if (camera == null) {
                    throw new IllegalStateException(
                        "Không tìm thấy webcam!"
                    );
                }

                camera.open();
                return camera;
            }

            @Override
            protected void done() {
                try {
                    webcam = get();

                    if (!isDisplayable()) {
                        webcam.close();
                        return;
                    }

                    webcamPanel = new WebcamPanel(webcam, false);
                    webcamPanel.setFPSDisplayed(true);
                    webcamPanel.setMirrored(true);

                    cameraContainer.removeAll();
                    cameraContainer.add(
                        webcamPanel, BorderLayout.CENTER
                    );

                    cameraContainer.revalidate();
                    cameraContainer.repaint();

                    webcamPanel.start();
                    btnDung.setEnabled(true);

                } catch (Exception ex) {
                    dangMo = false;
                    btnBatDau.setEnabled(true);

                    JOptionPane.showMessageDialog(
                        dlgQuetMaVach.this,
                        "Lỗi mở webcam: " + ex.getMessage()
                    );
                }
            }
        }.execute();
    }

    private void dungCamera() {
        if (!dangMo) return;

        dangMo = false;
        btnDung.setEnabled(false);
        btnBatDau.setEnabled(true);

        WebcamPanel panel = webcamPanel;
        Webcam camera = webcam;

        webcamPanel = null;
        webcam = null;

        cameraContainer.removeAll();
        cameraContainer.add(
            new JLabel("Camera đã dừng",
                SwingConstants.CENTER),
            BorderLayout.CENTER
        );

        cameraContainer.revalidate();
        cameraContainer.repaint();

        new Thread(() -> {
            if (panel != null) panel.stop();
            if (camera != null && camera.isOpen()) {
                camera.close();
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
            dlgQuetMaVach dialog =
                new dlgQuetMaVach(null);
            dialog.setVisible(true);
        });
    }
}

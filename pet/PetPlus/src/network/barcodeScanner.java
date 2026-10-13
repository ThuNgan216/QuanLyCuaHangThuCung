
package network;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class barcodeScanner {

    public static void main(String[] args) {

        Webcam webcam = null;
        MultiFormatReader reader = new MultiFormatReader();

        try {
            webcam = Webcam.getDefault();

            if (webcam == null) {
                System.out.println("Khong tim thay webcam!");
                return;
            }

            webcam.open();

            System.out.println("Webcam da mo!");
            System.out.println("Dua ma vach truoc camera...");

            Map<DecodeHintType, Object> hints =
                new EnumMap<>(DecodeHintType.class);

            hints.put(
                DecodeHintType.POSSIBLE_FORMATS,
                Arrays.asList(
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.QR_CODE
                )
            );

            hints.put(DecodeHintType.TRY_HARDER, true);
            reader.setHints(hints);

            while (!Thread.currentThread().isInterrupted()) {
                BufferedImage image = webcam.getImage();

                if (image == null) {
                    Thread.sleep(100);
                    continue;
                }

                try {
                    BinaryBitmap bitmap = new BinaryBitmap(
                        new HybridBinarizer(
                            new BufferedImageLuminanceSource(image)
                        )
                    );

                    Result result = reader.decodeWithState(bitmap);

                    System.out.println(
                        "Ma vach: " + result.getText()
                    );

                    break;

                } catch (NotFoundException e) {
                    // Tiep tuc quet
                } finally {
                    reader.reset();
                }

                Thread.sleep(100);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        }
    }
}

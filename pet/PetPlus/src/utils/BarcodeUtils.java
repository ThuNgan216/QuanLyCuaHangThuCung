
package utils;

import java.awt.image.BufferedImage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

public class BarcodeUtils {

    public static BufferedImage taoMaVach(String maVach)
            throws Exception {

        if (maVach == null || maVach.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Mã vạch không được để trống"
            );
        }

        BitMatrix matrix = new MultiFormatWriter().encode(
            maVach.trim(),
            BarcodeFormat.CODE_128,
            400,
            120
        );

        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}

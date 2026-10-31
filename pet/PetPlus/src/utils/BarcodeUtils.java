
package utils;

import java.awt.image.BufferedImage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.util.EnumMap;
import java.util.Map;

public class BarcodeUtils {

    public static BufferedImage taoMaVach(String maVach)
            throws Exception {

        if (maVach == null || maVach.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Mã vạch không được để trống"
            );
        }

        // Mã vạch luôn mã hóa đúng nội dung đã lưu trong MaVach; chừa quiet zone rộng
        // để camera không đọc nhầm vạch sát mép khi tem được in hoặc hiển thị trên màn hình.
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 20);
        BitMatrix matrix = new MultiFormatWriter().encode(
                maVach.trim(),
                BarcodeFormat.CODE_128,
                800,
                200,
                hints
        );

        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}

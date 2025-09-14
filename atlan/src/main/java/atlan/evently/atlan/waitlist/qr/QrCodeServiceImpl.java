// src/main/java/atlan/evently/atlan/waitlist/qr/QrCodeServiceImpl.java
package atlan.evently.atlan.waitlist.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrCodeServiceImpl implements QrCodeService {

    @Override
    public byte[] pngForUrl(String url, int size) {
        try {
            var writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size);
            try (var baos = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate QR code", e);
        }
    }
}

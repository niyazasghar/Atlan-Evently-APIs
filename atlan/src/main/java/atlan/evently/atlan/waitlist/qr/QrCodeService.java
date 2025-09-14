// src/main/java/atlan/evently/atlan/waitlist/qr/QrCodeService.java
package atlan.evently.atlan.waitlist.qr;

public interface QrCodeService {
    byte[] pngForUrl(String url, int size);
}

// src/main/java/.../idempotency/IdempotencyUtil.java
package atlan.evently.atlan.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class IdempotencyUtil {
    private static final HexFormat HEX = HexFormat.of();
    public static String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HEX.formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    private IdempotencyUtil() {}
}

// src/main/java/atlan/evently/atlan/waitlist/controller/QrWaitlistController.java
package atlan.evently.atlan.waitlist.controller;

import atlan.evently.atlan.waitlist.qr.QrCodeService;
import atlan.evently.atlan.waitlist.service.WaitlistService;
import atlan.evently.atlan.waitlist.service.WaitlistQueryService;
import atlan.evently.atlan.waitlist.web.dto.QueueStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/waitlist/qr")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class QrWaitlistController {

    private final QrCodeService qr;
    private final WaitlistService waitlistService;
    private final WaitlistQueryService waitlistQuery;

    public QrWaitlistController(QrCodeService qr,
                                WaitlistService waitlistService,
                                WaitlistQueryService waitlistQuery) {
        this.qr = qr;
        this.waitlistService = waitlistService;
        this.waitlistQuery = waitlistQuery;
    }

    // Build absolute base URL from request headers (supports proxies)
    private String baseUrl(HttpServletRequest req) {
        String proto = req.getHeader("X-Forwarded-Proto");
        String host = req.getHeader("X-Forwarded-Host");
        if (proto != null && host != null) return proto + "://" + host;
        return req.getScheme() + "://" + req.getServerName() +
                ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort());
    }

    @Operation(summary = "QR image to join event waitlist")
    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrImage(@RequestParam Long eventId,
                                          @RequestParam(defaultValue = "320") int size,
                                          HttpServletRequest request) {
        String joinUrl = baseUrl(request) + "/api/v1/waitlist/join?eventId=" + eventId;
        byte[] png = qr.pngForUrl(joinUrl, Math.max(160, Math.min(size, 1024)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    @Operation(summary = "Join event waitlist (current user)")
    @PostMapping("/join")
    public ResponseEntity<Void> join(@RequestParam Long eventId) {
        waitlistService.enqueueForCurrentUser(eventId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "My waitlist status (current user)")
    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> myStatus(@RequestParam Long eventId) {
        return ResponseEntity.ok(waitlistQuery.myStatusForEvent(eventId));
    }
}

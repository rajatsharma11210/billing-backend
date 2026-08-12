package com.expensetracker.billing_backend.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.billing_backend.service.GooglePlayService;
import com.google.api.services.androidpublisher.model.SubscriptionPurchaseV2;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final GooglePlayService googlePlayService;

    public SubscriptionController(GooglePlayService googlePlayService) {
        this.googlePlayService = googlePlayService;
    }

    @PostMapping("/verify")
public ResponseEntity<?> verifySubscription(
        @RequestBody Map<String, String> request) {

    try {
        String purchaseToken = request.get("purchaseToken");

        if (purchaseToken == null || purchaseToken.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "purchaseToken is required")
            );
        }

        SubscriptionPurchaseV2 purchase =
                googlePlayService.verifySubscription(purchaseToken);

        String subscriptionState =
                purchase.getSubscriptionState();

        String expiryTime = null;

        if (purchase.getLineItems() != null &&
                !purchase.getLineItems().isEmpty()) {

            expiryTime =
                    purchase.getLineItems()
                            .get(0)
                            .getExpiryTime();
        }

        boolean premiumActive = false;

        if (expiryTime != null && !expiryTime.isBlank()) {
            try {
                Instant expiry = Instant.parse(expiryTime);
                premiumActive = expiry.isAfter(Instant.now());
            } catch (Exception e) {
                premiumActive = false;
            }
        }

        // Google subscription states where access should not be granted.
        if ("SUBSCRIPTION_STATE_EXPIRED".equals(subscriptionState)
                || "SUBSCRIPTION_STATE_ON_HOLD".equals(subscriptionState)
                || "SUBSCRIPTION_STATE_PAUSED".equals(subscriptionState)
                || "SUBSCRIPTION_STATE_PENDING".equals(subscriptionState)) {

            premiumActive = false;
        }

        Map<String, Object> response = new HashMap<>();

        response.put("verified", true);
        response.put("premiumActive", premiumActive);
        response.put("subscriptionState", subscriptionState);
        response.put("expiryTime", expiryTime);
        response.put("acknowledgementState",
                purchase.getAcknowledgementState());
        response.put("regionCode",
                purchase.getRegionCode());
        response.put("startTime",
                purchase.getStartTime());
        response.put("lineItems",
                purchase.getLineItems());

        return ResponseEntity.ok(response);

    } catch (Exception e) {

        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "verified", false,
                        "error", e.getMessage()
                ));
    }
}
}
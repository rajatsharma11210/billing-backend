package com.expensetracker.billing_backend.service;
import org.springframework.stereotype.Service;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.SubscriptionPurchaseV2;


@Service
public class GooglePlayService {
    private final  AndroidPublisher androidPublisher;

    public static final String PACKAGE_NAME = "com.rajat.expensetrackerpro";

    public GooglePlayService(AndroidPublisher androidPublisher) {
        this.androidPublisher = androidPublisher;
    }
    public SubscriptionPurchaseV2 verifySubscription( String purchaseToken) throws Exception {
      return  androidPublisher
      .purchases()
      .subscriptionsv2()            
          .get(PACKAGE_NAME, purchaseToken)
        .execute();
    }

}

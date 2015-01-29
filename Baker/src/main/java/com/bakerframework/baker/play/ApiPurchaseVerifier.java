/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 * Copyright (c) 2015. Tobias Strebitzer, Francisco Contreras, Holland Salazar.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * Neither the name of the Baker Framework nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package com.bakerframework.baker.play;

import android.support.annotation.NonNull;

import com.bakerframework.baker.handler.ApiRequestHandler;
import com.bakerframework.baker.settings.Configuration;

import org.solovyev.android.checkout.BasePurchaseVerifier;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;

import java.util.List;

public class ApiPurchaseVerifier extends BasePurchaseVerifier {

    public ApiPurchaseVerifier() {
        super();
    }

    @Override
    protected void doVerify(@NonNull final List<Purchase> purchases, @NonNull final RequestListener<List<Purchase>> listener) {

        // Prepare params
        Purchase purchase = purchases.get(0);
        boolean isSubscription = Configuration.getSubscriptionProductIds().contains(purchase.sku);
        String purchaseType = isSubscription ? "subscription" : "product";

        // Post purchase verification request
        ApiRequestHandler apiRequestHandler = new ApiRequestHandler(Configuration.getPurchaseConfirmationUrl(purchaseType));
        PurchasesVerificationPayload purchasesVerificationPayload = new PurchasesVerificationPayload(purchase);
        boolean result = apiRequestHandler.post(purchasesVerificationPayload);

        // Trigger success
        if(result) {
            listener.onSuccess(purchases);
        }else{
            listener.onError(apiRequestHandler.getStatusCode(), new PurchaseVerificationException(apiRequestHandler.getResponseText()));
        }

    }

    private class PurchaseVerificationException extends Exception {
        public PurchaseVerificationException(String message) {
            super(message);
        }
    }

    private class PurchasesVerificationPayload {
        public final String data;
        public final String order_id;
        public final String package_name;
        public final String payload;
        public final String signature;
        public final String sku;
        public final String state;
        public final String token;
        public final long time;

        public PurchasesVerificationPayload(Purchase purchase) {
            this.data = purchase.data;
            this.order_id = purchase.orderId;
            this.package_name = purchase.packageName;
            this.payload = purchase.payload;
            this.signature = purchase.signature;
            this.sku = purchase.sku;
            this.state = purchase.state.toString();
            this.token = purchase.token;
            this.time = purchase.time;
        }
    }
}

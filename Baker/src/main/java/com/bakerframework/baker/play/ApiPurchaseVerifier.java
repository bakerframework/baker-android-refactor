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

import com.bakerframework.baker.settings.Configuration;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.solovyev.android.checkout.BasePurchaseVerifier;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;

import java.io.IOException;
import java.util.List;

public class ApiPurchaseVerifier extends BasePurchaseVerifier {

    public ApiPurchaseVerifier() {

    }

    @Override
    protected void doVerify(List<Purchase> purchases, RequestListener<List<Purchase>> listRequestListener) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost request = new HttpPost(Configuration.getPurchaseConfirmationUrl());

        try {
            // Build parameters
            Gson gson = new Gson();
            String payload = "{\"payload\":" + gson.toJson(purchases) + ",\"user_id\":\"" + Configuration.getUserId() + "\",\"source\":\"android-" + Configuration.getAppVersion() + "\"}";

            StringEntity params =new StringEntity(payload);
            request.addHeader("content-type", "application/json;charset=UTF-8");
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204) {
                // Server validated all purchases: trigger success
                listRequestListener.onSuccess(purchases);
            }else{
                // Server throws an error: trigger error
                String errorMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
                listRequestListener.onError(statusCode, new PurchaseVerificationException(errorMessage));
            }
        } catch (ClientProtocolException e) {
            listRequestListener.onError(400, e);
        } catch (IOException e) {
            listRequestListener.onError(400, e);
        }

    }

    private class PurchaseVerificationException extends Exception {
        public PurchaseVerificationException() { super(); }
        public PurchaseVerificationException(String message) { super(message); }
        public PurchaseVerificationException(String message, Throwable cause) { super(message, cause); }
        public PurchaseVerificationException(Throwable cause) { super(cause); }
    }

}

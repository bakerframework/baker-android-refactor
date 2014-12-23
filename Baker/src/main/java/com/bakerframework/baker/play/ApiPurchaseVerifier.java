package com.bakerframework.baker.play;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.bakerframework.baker.settings.Configuration;
import com.google.gson.Gson;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.solovyev.android.checkout.BasePurchaseVerifier;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.BillingException;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.PurchaseVerifier;
import org.solovyev.android.checkout.RequestListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias Strebitzer <tobias.strebitzer@magloft.com> on 21/12/14.
 * http://www.magloft.com
 */
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

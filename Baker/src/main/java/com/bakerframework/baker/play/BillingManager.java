package com.bakerframework.baker.play;

import android.app.Activity;
import android.util.Log;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.util.IabHelper;
import com.bakerframework.baker.util.IabResult;
import com.bakerframework.baker.util.Inventory;
import com.bakerframework.baker.util.Purchase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias Strebitzer <tobias.strebitzer@magloft.com> on 20/12/14.
 * http://www.magloft.com
 */
public class BillingManager implements IabHelper.QueryInventoryFinishedListener, IabHelper.OnIabPurchaseFinishedListener {

    // public static final String SKU_SUBSCRIPTION = "com.magloft.demo.subscription";

    IabHelper billingHelper;
    private BillingManagerDelegate delegate;
    private List<String> skuList;
    private List<String> purchasedSkuList;
    private List<String> storeSkuList;

    public BillingManager() {
        this.billingHelper = new IabHelper(BakerApplication.getInstance(), BakerApplication.getInstance().getEncodedPublicKey());
    }

    public void setDelegate(BillingManagerDelegate delegate) {
        this.delegate = delegate;
    }

    public List<String> getSkuList() {
        return storeSkuList;
    }
    public List<String> getStoreSkuList() {
        return storeSkuList;
    }
    public List<String> getPurchasedSkuList() {
        return purchasedSkuList;
    }

    public boolean isSkuPurchased(String sku) {
        return purchasedSkuList.contains(sku);
    }

    public void setup() {
        billingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    delegate.onBillingSetupError(result);
                }else{
                    delegate.onBillingSetupSuccess(result);
                }
            }
        });
    }

    public void loadInventory(List<String> skuList) {

        // Reset purchased sku list
        this.skuList = skuList;

        // Load inventory
        billingHelper.queryInventoryAsync(true, skuList, this);
    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
        this.purchasedSkuList = new ArrayList<>();
        this.storeSkuList = new ArrayList<>();

        if (!result.isFailure()) {
            // Check for purchased skus
            for(String sku : skuList) {
                if(inv.hasDetails(sku)) {
                    storeSkuList.add(sku);
                }
                if(inv.hasPurchase(sku)) {
                    purchasedSkuList.add(sku);
                }
            }
            delegate.onInventoryLoaded(result, inv);
        }else{
            delegate.onInventoryError(result);
        }
    }

    // Purchases / Billing

    public void purchase(Activity activity, String sku, int requestCode) {
        billingHelper.launchPurchaseFlow(activity, sku, requestCode, this);
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        Log.i("BILLING", "Purchase finished: " + result + "; info: " + purchase);
        if (result.isSuccess()) {
            Log.i("BILLING", "Purchase successful");
            // Successful - the item has been payed for

            // We set a vale in the shared preferences to mark this app as
            // being the premium version
            // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BuyPremiumActivity.this);
            // prefs.edit().putBoolean(PremiumHandler.KEY_PREMIUM_VERSION, true).apply();

            // Start the Settings Activity together withe the info, that
            // the user just bought this app - in this case we show a
            // "thank you" Dialog.
        }
    }

    // Cleanup
    public void dispose() {
        if (billingHelper != null) billingHelper.dispose();
        billingHelper = null;
    }

}

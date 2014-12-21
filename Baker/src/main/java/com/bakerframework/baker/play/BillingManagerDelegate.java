package com.bakerframework.baker.play;

import com.bakerframework.baker.util.IabResult;
import com.bakerframework.baker.util.Inventory;

/**
 * Created by Tobias Strebitzer <tobias.strebitzer@magloft.com> on 15/12/14.
 * http://www.magloft.com
 */
public interface BillingManagerDelegate {
    void onBillingSetupSuccess(IabResult result);
    void onBillingSetupError(IabResult result);
    void onInventoryLoaded(IabResult result, Inventory inv);
    void onInventoryError(IabResult result);
}

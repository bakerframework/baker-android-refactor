package com.bakerframework.baker.play;

import android.app.Activity;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.settings.Configuration;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

/**
 * Created by Tobias Strebitzer <tobias.strebitzer@magloft.com> on 21/12/14.
 * http://www.magloft.com
 */
public class LicenceManager {

    // Licencing
    private static final byte[] SALT = new byte[] {
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95,
            -45, 77, -117, -36, -113, -11, 32, -64, 89
    };
    private LicenseCheckerCallback licenseCheckerCallback;
    private LicenseChecker licenseChecker;
    private LicenceManagerDelegate delegate;

    public LicenceManager() {
        licenseCheckerCallback = new AppLicenseCheckerCallback();
        licenseChecker = new LicenseChecker(BakerApplication.getInstance(), new ServerManagedPolicy(BakerApplication.getInstance(), new AESObfuscator(SALT, BakerApplication.getInstance().getPackageName(), Configuration.getUserId())), BakerApplication.getInstance().getString(R.string.google_play_license_key));
    }

    public void setDelegate(LicenceManagerDelegate delegate) {
        this.delegate = delegate;
    }

    public void checkAccess() {
        licenseChecker.checkAccess(licenseCheckerCallback);
    }

    private class AppLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int reason) {
            if (((Activity) delegate).isFinishing()) { return; }
            delegate.onLicenceValid(reason);
        }


        public void dontAllow(int reason) {
            if (((Activity) delegate).isFinishing()) { return; }
            if (reason == Policy.RETRY) {
                delegate.onLicenceRetry(reason);
            } else {
                delegate.onLicenceInvalid(reason);
            }
        }

        @Override
        public void applicationError(int reason) {
            delegate.onLicenceInvalid(reason);
        }
    }

}

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

import android.app.Activity;

import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;
import com.bakerframework.baker.settings.Configuration;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

public class LicenceManager {

    // Licencing
    private static final byte[] SALT = new byte[] {
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95,
            -45, 77, -117, -36, -113, -11, 32, -64, 89
    };
    private final LicenseCheckerCallback licenseCheckerCallback;
    private LicenceManagerDelegate delegate;

    public LicenceManager() {
        licenseCheckerCallback = new AppLicenseCheckerCallback();
    }

    public void setDelegate(LicenceManagerDelegate delegate) {
        this.delegate = delegate;
    }

    public void checkAccess() {
        LicenseChecker licenseChecker = new LicenseChecker(BakerApplication.getInstance(), new ServerManagedPolicy(BakerApplication.getInstance(), new AESObfuscator(SALT, BakerApplication.getInstance().getPackageName(), Configuration.getUserId())), BakerApplication.getInstance().getString(R.string.google_play_license_key));
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

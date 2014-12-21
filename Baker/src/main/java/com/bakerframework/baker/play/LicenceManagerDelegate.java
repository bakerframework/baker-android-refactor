package com.bakerframework.baker.play;

/**
 * Created by Tobias Strebitzer <tobias.strebitzer@magloft.com> on 21/12/14.
 * http://www.magloft.com
 */
public interface LicenceManagerDelegate  {
    void onLicenceValid(int reason);
    void onLicenceInvalid(int reason);
    void onLicenceRetry(int reason);
}

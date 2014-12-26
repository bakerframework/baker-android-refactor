package com.bakerframework.baker.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bakerframework.baker.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Tobias Strebitzer <tobias.strebitzer@magloft.com> on 26/12/14.
 * http://www.magloft.com
 */
public class ImageLoaderHelper {


    private ImageLoaderHelper() {}

    public static ImageLoader getImageLoader(Context context){
        try {
            if (ImageLoader.getInstance().isInited()) {
                return ImageLoader.getInstance();
            }
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.loading)
                    .showImageForEmptyUri(R.drawable.loading)
                    .showImageOnFail(R.drawable.loading).cacheInMemory()
                    .cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                    context)
                    .defaultDisplayImageOptions(defaultOptions).build();
            ImageLoader.getInstance().init(config);
            return ImageLoader.getInstance();
        } catch (Exception ex) {
            Log.e("ImageLoaderHelper", "Error when get image loader instance: " + ex);
            return null;
        }
    }
}

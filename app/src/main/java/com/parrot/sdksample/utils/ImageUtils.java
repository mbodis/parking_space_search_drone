package com.parrot.sdksample.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mbodis on 4/21/17.
 */

public class ImageUtils {

    public static File getImgByUuid(Context ctx, String uuid) {
        (new File(ctx.getExternalFilesDir(null) + "/imgs", "")).mkdirs();
        File file = new File(ctx.getExternalFilesDir(null) + "/imgs", uuid
                + ".png");

        return file;
    }

    public static boolean saveBitmapToSdCard(Context ctx,
                                             Bitmap bitmap, String imgUuid
    ) {
        try {
            File file = getImgByUuid(ctx, imgUuid);

            if (file.exists()) {
                file.delete();
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                    new FileOutputStream(file.getAbsolutePath()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

package com.study.avatar.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

public class BitmapUtils {

    /**
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        Log.d("TAG", "convertViewToBitmap: makeMeasureSpec = " + View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        Log.d("TAG", "convertViewToBitmap: getMeasuredWidth = " + view.getMeasuredWidth() + ", getMeasuredHeight = " + view.getMeasuredHeight());
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        Log.d("TAG", "convertViewToBitmap: bitmap = " + bitmap);
        return bitmap;
    }

}
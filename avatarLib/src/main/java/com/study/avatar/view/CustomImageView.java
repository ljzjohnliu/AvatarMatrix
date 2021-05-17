package com.study.avatar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

public class CustomImageView extends AppCompatImageView {
    private Paint paint;

    Matrix matrix = new Matrix();

    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setVisibility(INVISIBLE);

        this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        canvas.concat(matrix);
        this.setLayerType(View.LAYER_TYPE_NONE, null);
        setVisibility(VISIBLE);
    }

    @Override
    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                postInvalidate();
            }
        });
    }

}

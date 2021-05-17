package com.study.avatar.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.study.avatar.R;
import com.study.avatar.util.BitmapUtils;
import com.study.avatar.util.LogUtils;

public class AvatarFrame extends FrameLayout {
    private Paint paint;
    private int mImageHeight = 150, mImageWidth = 150;

    private Bitmap bitmap;

    private Context context;
    private AvatarFrameOutsideLinearLayout layout;

    private float sx;
    private float sy;
    private float dx;
    private float dy;
    private float degrees;
    private float px;
    private float py;
    private float centerX;
    private float centerY;

    public PointF leftTop = new PointF();
    public PointF rightTop = new PointF();
    public PointF leftBottom = new PointF();
    public PointF rightBottom = new PointF();
    private boolean isSelect = false;

    public Bitmap bitMirror = null;
    public Bitmap bitDelete = null;
    public Bitmap bitMove = null;

    Matrix matrix = new Matrix();

    private int xiao;

    public AvatarFrame(Context context) {
        super(context);
        this.context = context;
        initPaint();
        addAvatarView();
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setWillNotDraw(false);
    }

    public AvatarFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initPaint();
        addAvatarView();
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setWillNotDraw(false);
    }

    public AvatarFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initPaint();
        addAvatarView();
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setWillNotDraw(false);
    }

    public AvatarFrameOutsideLinearLayout getLayout() {
        return layout;
    }

    private void addAvatarView() {
        layout = new AvatarFrameOutsideLinearLayout(context);
        layout.setImageUri();
        Log.d("TAG", "addAvatarView: layout = " + layout);
        bitmap = BitmapUtils.convertViewToBitmap(layout);

        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        Log.d("TAG", "addAvatarView: mImageWidth = " + mImageWidth + ", mImageHeight = " + mImageHeight);

        layout.setmImageWidth(mImageWidth);
        layout.setmImageHeight(mImageHeight);

        layout.setSelect(true);
//        layout.setBackgroundResource(R.drawable.img_font_frame);
        addView(layout);
    }

    public int getmImageHeight() {
        return mImageHeight;
    }

    public void setmImageHeight(int mImageHeight) {
        this.mImageHeight = mImageHeight;
    }

    public int getmImageWidth() {
        return mImageWidth;
    }

    public void setmImageWidth(int mImageWidth) {
        this.mImageWidth = mImageWidth;
    }

    public void postScale(float sx, float sy, float centerX, float centerY) {
        this.sx = sx;
        this.sy = sy;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void postTranslate(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public void postRotate(float degrees, float px, float py) {
        this.degrees = degrees;
        this.px = px;
        this.py = py;
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5f);

        if (bitMirror == null) {
            bitMirror = BitmapFactory.decodeResource(getResources(),
                    R.drawable.btn_sticker_turn_n);
        }
        if (bitDelete == null) {
            bitDelete = BitmapFactory.decodeResource(getResources(),
                    R.drawable.btn_sticker_cancel_n);
        }
        if (bitMove == null) {
            bitMove = BitmapFactory.decodeResource(getResources(),
                    R.drawable.btn_sticker_word_turn_n);
        }
/*

        if (bgBitmap == null) {
            bgBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.img_font_frame);
        }
*/

        xiao = bitDelete.getHeight() / 2;
//         mSetfil = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setVisibility(INVISIBLE);

        if (isSelect) {
            canvas.drawLine(leftTop.x, leftTop.y, rightTop.x, rightTop.y, paint);
            canvas.drawLine(leftBottom.x, leftBottom.y, rightBottom.x, rightBottom.y, paint);
            canvas.drawLine(leftTop.x, leftTop.y, leftBottom.x, leftBottom.y, paint);
            canvas.drawLine(rightTop.x, rightTop.y, rightBottom.x, rightBottom.y, paint);

            canvas.drawBitmap(bitMirror, leftTop.x - xiao, leftTop.y - xiao, paint);
            canvas.drawBitmap(bitDelete, rightTop.x - xiao, rightTop.y - xiao, paint);
            canvas.drawBitmap(bitMove, rightBottom.x - xiao, rightBottom.y - xiao, paint);
        }

        this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        canvas.concat(matrix);
        this.setLayerType(View.LAYER_TYPE_NONE, null);
        setVisibility(VISIBLE);
    }

    @Override
    public Matrix getMatrix() {
        return matrix;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
        if (select == false) {
//            this.getLayout().setBackgroundResource(0);
            /*Intent intent = new Intent(AppConstant.ACTION.POPVIEWADDWORD_CLOSE);
            context.sendBroadcast(intent);*/
        } else {
//            this.getLayout().setBackgroundResource(R.drawable.img_font_frame);
            /*Intent intent = new Intent(AppConstant.ACTION.POPVIEWADDWORD_SHOW);
            context.sendBroadcast(intent);*/
        }
//        invalidate();
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

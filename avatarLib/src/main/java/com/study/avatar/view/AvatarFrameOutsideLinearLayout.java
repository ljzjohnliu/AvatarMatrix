package com.study.avatar.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.study.avatar.R;

public class AvatarFrameOutsideLinearLayout extends RelativeLayout {
    private Context context;
    private Paint paint;
    private int mImageHeight;
    private int mImageWidth;

    private ImageView imageView;

    private boolean isSelect = false;

    public AvatarFrameOutsideLinearLayout(Context context) {
        super(context);

        this.context = context;
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(params);
        setWillNotDraw(false);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#F77DA3"));
        paint.setStrokeWidth(5f);

    }

    public AvatarFrameOutsideLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(params);
        setWillNotDraw(false);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#F77DA3"));
        paint.setStrokeWidth(5f);
    }

    public void setImageUri() {
        if (imageView == null) {
            imageView = new ImageView(context);
            addView(imageView);
        }
        imageView.setImageDrawable(context.getDrawable(R.drawable.test_avatar));
    }

    public ImageView getImageView() {
        return imageView;
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

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
        invalidate();
    }

    public interface OnLayoutWidth {
        void layout(int width, int height);
    }

    public void layoutWidthAndHeight(final AvatarFrameOutsideLinearLayout avatarFrameOutsideLinearLayout, final OnLayoutWidth onLayoutWidth) {
        avatarFrameOutsideLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    avatarFrameOutsideLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                onLayoutWidth.layout(avatarFrameOutsideLinearLayout.getMeasuredWidth(), avatarFrameOutsideLinearLayout.getMeasuredHeight());
            }
        });
    }
}

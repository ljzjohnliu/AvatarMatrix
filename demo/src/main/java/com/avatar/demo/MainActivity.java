package com.avatar.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.study.avatar.util.BitmapUtils;
import com.study.avatar.util.LogUtils;
import com.study.avatar.util.StatusBarHeightUtil;
import com.study.avatar.view.AvatarFrameHolder;
import com.study.avatar.view.AvatarFrameState;
import com.study.avatar.view.AvatarFrameOutsideLinearLayout;
import com.study.avatar.view.AvatarFrame;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    private FrameLayout frame;
    private int width;
    private int height;

    private AvatarFrameOutsideLinearLayout layout;
    private AvatarFrame avatarFrame;
    private int operateMode;
    private Bitmap operatingBitmap;
    private int operatedWidth;
    private int operatedHeight;
    private int avatarx1;
    private int avatary1;
    private Matrix avatarMatrix = new Matrix();
    private Matrix avatarSavedMatrix = new Matrix();
    private AvatarFrameHolder avatarFrameHolders;
    private int avatarSelectImageCount = -1;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        width = MyApplication.getInstance().getScreenWidth();
        height = MyApplication.getInstance().getScreenHeight();
        Log.d("TAG", "onCreate: width = " + width + ", height = " + height);
        initView();
        initData();
    }

    private void initData() {
        addMyFrame();
    }

    private void initView() {
        frame = (FrameLayout) findViewById(R.id.frame);
        findViewById(R.id.addView).setOnClickListener(this);
    }


    /**
     * 调整加字框的大小 以及删除和变换坐标
     */
    private void ajustAvatarFrame() {
        if (avatarSelectImageCount != -1) {
            avatarFrameHolders.getAvatarFrame().getLayout().layoutWidthAndHeight(avatarFrameHolders.getAvatarFrame().getLayout(), new AvatarFrameOutsideLinearLayout.OnLayoutWidth() {
                @Override
                public void layout(int width, int height) {
                    avatarMatrix = avatarFrameHolders.getAvatarFrame().getMatrix();
                    avatarSavedMatrix.set(avatarMatrix);
                    adjustLocation(avatarMatrix, avatarFrameHolders.getAvatarFrame());
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addView:
                addMyFrame();
                break;
        }
    }

    /**
     * 平移1个单位 只为调整位置
     *
     * @param matrix
     * @param avatarFrame
     */
    private void adjustLocation(Matrix matrix, AvatarFrame avatarFrame) {
        //将有缩放平移和旋转相关值的矩阵赋值到f中
        float[] f = new float[9];
        matrix.getValues(f);
        int bWidth = 0;
        int bHeight = 0;

        //取到view的宽高
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Rect rect = new Rect();
            avatarFrame.getLayout().getGlobalVisibleRect(rect);
            bWidth = rect.width();
            bHeight = rect.height();
        } else {
            bWidth = operatedWidth;
            bHeight = operatedHeight;
        }

        //如果想知道这里这样设置值的具体算法那必须要了解9*9的矩阵每个坐标的含义了 有兴趣的可以查阅一下 资料很多
        // 原图左上角
        float x1 = f[2];
        float y1 = f[5];
        avatarFrame.leftTop.set(x1, y1);
        // 原图右上角
        float x2 = f[0] * bWidth + f[2];
        float y2 = f[3] * bWidth + f[5];
        avatarFrame.rightTop.set(x2, y2);
        // 原图左下角
        float x3 = f[1] * bHeight + f[2];
        float y3 = f[4] * bHeight + f[5];
        avatarFrame.leftBottom.set(x3, y3);
        // 原图右下角
        float x4 = f[0] * bWidth + f[1] * bHeight + f[2];
        float y4 = f[3] * bWidth + f[4] * bHeight + f[5];
        avatarFrame.rightBottom.set(x4, y4);


        //这里一定要这是图片的最左 最右 最上 和 最下的位置 用来判断是不是点击到了当前的view
        // 最左边x
        float minX = 0;
        // 最右边x
        float maxX = 0;
        // 最上边y
        float minY = 0;
        // 最下边y
        float maxY = 0;

        minX = Math.min(x4, Math.min(x3, Math.min(x1, x2))) - 30;
        maxX = Math.max(x4, Math.max(x3, Math.max(x1, x2))) + 30;
        minY = Math.min(y4, Math.min(y3, Math.min(y1, y2))) - 30;
        maxY = Math.max(y4, Math.max(y3, Math.max(y1, y2))) + 30;

        avatarFrameHolders.getState().setLeft(minX);
        avatarFrameHolders.getState().setTop(minY);
        avatarFrameHolders.getState().setRight(maxX);
        avatarFrameHolders.getState().setBottom(maxY);

        //将当前的view设置上矩阵对象
        avatarFrameHolders.getAvatarFrame().setMatrix(matrix);
    }

    private void addMyFrame() {
        avatarFrame = new AvatarFrame(this);
        avatarFrame.setSelect(true);
        //add to your frame
        frame.addView(avatarFrame);

        layout = avatarFrame.getLayout();

        operatingBitmap = BitmapUtils.convertViewToBitmap(layout);

        operatedWidth = operatingBitmap.getWidth();
        operatedHeight = operatingBitmap.getHeight();

        //Set to the center of the screen and set the vertical and horizontal coordinates of four points
        avatarx1 = width / 2 - operatedWidth / 2;
        avatary1 = height / 3;
        avatarFrame.leftTop.set(avatarx1, avatary1);
        avatarFrame.rightTop.set(avatarx1 + operatedWidth, avatary1);
        avatarFrame.leftBottom.set(avatarx1, avatary1 + operatedHeight);
        avatarFrame.rightBottom.set(avatarx1 + operatedWidth, avatary1 + operatedHeight);


        //here use matrix to scaling gesture
        avatarMatrix = new Matrix();
        avatarMatrix.postTranslate(avatarx1, avatary1);
        avatarFrame.setMatrix(avatarMatrix);

        //Here for each view with a rectangular package , click the rectangle on selected current view
        AvatarFrameState avatarFrameState = new AvatarFrameState();
        avatarFrameState.setLeft(avatarx1);
        avatarFrameState.setTop(avatary1);
        avatarFrameState.setRight(avatarx1 + operatedWidth);
        avatarFrameState.setBottom(avatary1 + operatedHeight);

        avatarFrameHolders = new AvatarFrameHolder();
        avatarFrameHolders.setAvatarFrame(avatarFrame);
        avatarFrameHolders.setState(avatarFrameState);

        avatarFrame.setOnTouchListener(new AddWordMyOntouch());
        avatarSelectImageCount = 0;
    }

    private void selectMyFrame(float x, float y) {
        if (avatarFrameHolders.getAvatarFrame().isSelect()) {
            avatarFrameHolders.getAvatarFrame().setSelect(false);
        }

        //Create a rectangular area here getLeft getTop etc. mean the current view of the leftmost
        // uppermost and lowermost rightmost only to click inside the region is selected
        Rect rect = new Rect((int) avatarFrameHolders.getState().getLeft(),
                (int) avatarFrameHolders.getState().getTop(),
                (int) avatarFrameHolders.getState().getRight(),
                (int) avatarFrameHolders.getState().getBottom());

        if (rect.contains((int) x, (int) y)) {
            //If you select the current view mentioned uppermost layer
            avatarFrameHolders.getAvatarFrame().bringToFront();
            avatarFrameHolders.getAvatarFrame().setSelect(true);
            //Which record is selected
            avatarSelectImageCount = 0;
            LogUtils.e("selected");
        } else {
            avatarSelectImageCount = -1;
            LogUtils.e("no select");
        }
    }

    class AddWordMyOntouch implements View.OnTouchListener {
        private float baseValue = 0;
        //The original angle
        private float oldRotation;
        //旋转和缩放的中点
        private PointF midP;
        //点中的要进行缩放的点与图片中点的距离
        private float imgLengHalf;
        //保存刚开始按下的点
        private PointF startPoinF = new PointF();

        private int NONE = 0; // 无
        private int DRAG = 1; // 移动
        private int ZOOM = 2; // 变换
        private int DOUBLE_ZOOM = 3;
        private int MIRROR = 4;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eventaction = event.getAction();
            float event_x = (int) event.getRawX();
            float event_y = (int) event.getRawY() - StatusBarHeightUtil.getStatusBarHeight(context);

            //这里算是一个点击区域值 点中删除或者点中变换的100 * 100 的矩形区域 用这个区域来判断是否点中
            int tempInt = 50;
            int addint = 50;

            switch (eventaction & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: // touch down so check if the

                    baseValue = 0;

                    startPoinF.set(event_x, event_y);// 保存刚开始按下的坐标

                    //因为可能要添加多个这样的view 所以要按选中了哪个
                    selectMyFrame(event_x, event_y);

                    //如果有选中状态的额view
                    if (avatarSelectImageCount != -1) {
                        avatarFrame = avatarFrameHolders.getAvatarFrame();
                        avatarMatrix = avatarFrameHolders.getAvatarFrame().getMatrix();
                        avatarSavedMatrix.set(avatarMatrix);
                        operateMode = DRAG;

                        //构造一个旋转按钮的矩形区域
                        Rect moveRect = new Rect((int) avatarFrame.rightBottom.x - tempInt,
                                (int) avatarFrame.rightBottom.y - tempInt, (int) avatarFrame.rightBottom.x + addint,
                                (int) avatarFrame.rightBottom.y + addint);
                        //删除按钮的矩形区域
                        Rect mirrorRect = new Rect((int) avatarFrame.leftTop.x - tempInt,
                                (int) avatarFrame.leftTop.y - tempInt, (int) avatarFrame.leftTop.x + addint,
                                (int) avatarFrame.leftTop.y + addint);
                        //删除按钮的矩形区域
                        Rect deleteRect = new Rect((int) avatarFrame.rightTop.x - tempInt,
                                (int) avatarFrame.rightTop.y - tempInt, (int) avatarFrame.rightTop.x + addint,
                                (int) avatarFrame.rightTop.y + addint);


                        //如果点中了变换
                        if (moveRect.contains((int) event_x, (int) event_y)) {
                            LogUtils.e("点中了缩放旋转变换");
                            // 点中了变换
                            midP = midPoint(avatarFrame.leftTop, avatarFrame.rightBottom);
                            imgLengHalf = spacing(midP, avatarFrame.rightBottom);
                            oldRotation = rotation(midP, startPoinF);
                            operateMode = ZOOM;
                        } else if (mirrorRect.contains((int) event_x, (int) event_y)) {
                            // 点中了删除
                            LogUtils.e("点中了镜像变换");
                            midP = midPoint(avatarFrame.leftTop, avatarFrame.rightBottom);
                            imgLengHalf = spacing(midP, avatarFrame.rightBottom);
                            oldRotation = rotation(midP, startPoinF);
                            operateMode = MIRROR;

                            if (operateMode != NONE) {
                                if (avatarSelectImageCount != -1) {
                                    //最后在action_move 执行完前设置好矩阵 设置view的位置
                                    avatarFrame = avatarFrameHolders.getAvatarFrame();
                                    avatarMatrix.set(avatarSavedMatrix);
                                    avatarMatrix.postScale(-1, 1, midP.x, midP.y);
//                                    avatarMatrix.postScale(scale, scale, midP.x, midP.y);
//                                    avatarMatrix.postRotate(newRotation, midP.x, midP.y);
                                    adjustLocation(avatarMatrix, avatarFrame);
                                }
                            }

                        } else if (deleteRect.contains((int) event_x, (int) event_y)) {
                            // 点中了删除
                            LogUtils.e("点中了删除");
                            deleteMyFrame();
                        }
                    }
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    operateMode = NONE;
                    if (avatarSelectImageCount != -1) {
                        midP = midPoint(avatarFrame.leftTop, avatarFrame.rightBottom);
                        imgLengHalf = spacing(midP, avatarFrame.rightBottom);
                        oldRotation = rotationforTwo(event);
                    }
                    break;

                case MotionEvent.ACTION_MOVE: // touch drag with the ball
                    //如果是双指点中
                    if (event.getPointerCount() == 2) {
                        if (avatarSelectImageCount != -1) {
                            operateMode = DOUBLE_ZOOM;
                            float x = event.getX(0) - event.getX(1);
                            float y = event.getY(0) - event.getY(1);
                            float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离

                            //旋转的角度
                            float newRotation = rotationforTwo(event) - oldRotation;
                            if (baseValue == 0) {
                                baseValue = value;
                            } else {
                                //旋转到一定角度再执行 不能刚点击就执行旋转或者缩放
                                if (value - baseValue >= 15 || value - baseValue <= -15) {
                                    float scale = value / baseValue;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。
                                    avatarMatrix.set(avatarSavedMatrix);
                                    avatarMatrix.postScale(scale, scale, midP.x, midP.y);
                                    avatarMatrix.postRotate(newRotation, midP.x, midP.y);
                                }
                            }
                        }
                    } else if (event.getPointerCount() == 1) {
                        //单指点击
                        if (avatarSelectImageCount != -1) {
                            if (operateMode == DRAG) {
                                if (event_x < MyApplication.getInstance().getScreenWidth() - 50 && event_x > 50
                                        && event_y > 100
                                        && event_y < MyApplication.getInstance().getScreenHeight() - 100) {
                                    avatarMatrix.set(avatarSavedMatrix);
                                    // 图片移动的距离
                                    float translateX = event_x - startPoinF.x;
                                    float translateY = event_y - startPoinF.y;
                                    avatarMatrix.postTranslate(translateX, translateY);
                                }
                            } else if (operateMode == ZOOM) {
                                //点击到了缩放旋转按钮
                                PointF movePoin = new PointF(event_x, event_y);

                                float moveLeng = spacing(startPoinF, movePoin);
                                float newRotation = rotation(midP, movePoin) - oldRotation;

                                if (moveLeng > 10f) {
                                    float moveToMidLeng = spacing(midP, movePoin);
                                    float scale = moveToMidLeng / imgLengHalf;

                                    avatarMatrix.set(avatarSavedMatrix);
                                    avatarMatrix.postScale(scale, scale, midP.x, midP.y);
                                    avatarMatrix.postRotate(newRotation, midP.x, midP.y);
                                }
                            }
                        }
                    }

                    if (operateMode != NONE && operateMode != MIRROR) {
                        if (avatarSelectImageCount != -1) {
                            //最后在action_move 执行完前设置好矩阵 设置view的位置
                            avatarFrame = avatarFrameHolders.getAvatarFrame();
                            adjustLocation(avatarMatrix, avatarFrame);
                        }
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP: //一只手指离开屏幕，但还有一只手指在上面会触此事件
                    //什么都没做
                    operateMode = NONE;
                    break;

                case MotionEvent.ACTION_UP:
                    operateMode = NONE;
                    break;
            }
            return true;
        }
    }

    private void deleteMyFrame() {
        if (avatarSelectImageCount != -1) {
            frame.removeView(avatarFrameHolders.getAvatarFrame());
            avatarFrameHolders = null;
            avatarSelectImageCount = -1;
        }
    }

    private float rotationforTwo(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    // 得到两个点的距离
    private float spacing(PointF p1, PointF p2) {
        float x = p1.x - p2.x;
        float y = p1.y - p2.y;
        return (float) Math.sqrt(x * x + y * y);
    }

    // 得到两个点的中点
    private PointF midPoint(PointF p1, PointF p2) {
        PointF point = new PointF();
        float x = p1.x + p2.x;
        float y = p1.y + p2.y;
        point.set(x / 2, y / 2);
        return point;
    }

    // 旋转
    private float rotation(PointF p1, PointF p2) {
        double delta_x = (p1.x - p2.x);
        double delta_y = (p1.y - p2.y);
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

}

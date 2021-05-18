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
import android.widget.Button;
import android.widget.FrameLayout;

import com.study.avatar.util.BitmapUtils;
import com.study.avatar.util.LogUtils;
import com.study.avatar.util.StatusBarHeightUtil;
import com.study.avatar.view.AvatarFrame;
import com.study.avatar.view.AvatarFrameOutsideLinearLayout;

public class MainActivity extends Activity implements View.OnClickListener {
    public static final int NONE = 0; // 无
    public static final int DRAG = 1; // 移动
    public static final int ZOOM = 2; // 变换
    public static final int DOUBLE_ZOOM = 3;//双指变换
    public static final int MIRROR = 4; //镜像

    private FrameLayout avatarContainer;
    private int screenWidth;
    private int screenHeight;

    private Context context;

    private boolean isShowOptBtn = false;

    private AvatarFrameOutsideLinearLayout layout;
    private AvatarFrame avatarFrame;

    private int operateMode;
    private Bitmap operatingBitmap;
    private int operatedWidth;
    private int operatedHeight;
    private int avatarx1;
    private int avatary1;
    private Matrix avatarMatrix;
    private Matrix avatarSavedMatrix = new Matrix();

    private Button addViewBtn;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addView:
                addMyFrame();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        screenWidth = MyApplication.getInstance().getScreenWidth();
        screenHeight = MyApplication.getInstance().getScreenHeight();
        Log.d("TAG", "onCreate: screenWidth = " + screenWidth + ", screenHeight = " + screenHeight);
        initView();
        initData();
    }

    private void initView() {
        avatarContainer = (FrameLayout) findViewById(R.id.avatar_container);
        addViewBtn = (Button) findViewById(R.id.addView);
        findViewById(R.id.addView).setOnClickListener(this);
    }

    private void initData() {
        addMyFrame();
    }

    private void addMyFrame() {
        avatarFrame = new AvatarFrame(this);
        avatarFrame.setSelect(true);
        avatarContainer.addView(avatarFrame);

        layout = avatarFrame.getLayout();
        Log.d("TAG", "addMyFrame: layout getImageView = " + layout.getImageView());

        operatingBitmap = BitmapUtils.convertViewToBitmap(layout/*.getImageView()*/);

        operatedWidth = operatingBitmap.getWidth();
        operatedHeight = operatingBitmap.getHeight();
        Log.d("TAG", "addMyFrame: 22 operatedWidth = " + operatedWidth + ", operatedHeight = " + operatedHeight);

        //Set to the center of the screen and set the vertical and horizontal coordinates of four points
        avatarx1 = screenWidth / 2 - operatedWidth / 2;
        avatary1 = screenHeight / 3;
        avatarFrame.leftTop.set(avatarx1, avatary1);
        avatarFrame.rightTop.set(avatarx1 + operatedWidth, avatary1);
        avatarFrame.leftBottom.set(avatarx1, avatary1 + operatedHeight);
        avatarFrame.rightBottom.set(avatarx1 + operatedWidth, avatary1 + operatedHeight);


        //here use matrix to scaling gesture
        avatarMatrix = new Matrix();
        avatarMatrix.postTranslate(avatarx1, avatary1);
        avatarFrame.setMatrix(avatarMatrix);
//        avatarFrame.getLayout().getImageView().setMatrix(avatarMatrix);
//        avatarFrame.getLayout().getImageView().setImageMatrix(avatarMatrix);

        avatarFrame.setOnTouchListener(new AvatarMyOntouch());
        isShowOptBtn = true;
        adjustLocation(avatarMatrix, avatarFrame);
        Log.d("TAG", "addMyFrame: avatarSelectImageCount = " + isShowOptBtn);
    }


    //这里一定要这是图片的最左 最右 最上 和 最下的位置 用来判断是不是点击到了当前的view
    // 最左边x
    float minX = 0;
    // 最右边x
    float maxX = 0;
    // 最上边y
    float minY = 0;
    // 最下边y
    float maxY = 0;

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

        minX = Math.min(x4, Math.min(x3, Math.min(x1, x2))) - 30;
        maxX = Math.max(x4, Math.max(x3, Math.max(x1, x2))) + 30;
        minY = Math.min(y4, Math.min(y3, Math.min(y1, y2))) - 30;
        maxY = Math.max(y4, Math.max(y3, Math.max(y1, y2))) + 30;
        Log.d("TAG", "adjustLocation: minX = " + minX + ", maxX = " + maxX + ", minY = " + minY + ", maxY = " + maxY);

        Log.d("TAG", "adjustLocation: operateMode = " + operateMode);

        //将当前的view设置上矩阵对象
        switch (operateMode) {
            case DRAG:
            case ZOOM:
            case DOUBLE_ZOOM:
                avatarFrame.setMatrix(matrix);
                break;
            case MIRROR:
                avatarFrame.getLayout().getImageView().setMatrix(matrix);
                break;
            default:
                break;
        }
    }

    private void selectMyFrame(float x, float y) {
        Log.d("TAG", "selectMyFrame: x = " + x + ", y = " + y + ", isSelect = " + avatarFrame.isSelect());

        //Create a rectangular area here getLeft getTop etc. mean the current view of the leftmost
        // uppermost and lowermost rightmost only to click inside the region is selected
        //如下获取avatarFrame的尺寸是全屏幕的！！
        Log.d("TAG", "selectMyFrame: avatarFrame left = " + avatarFrame.getLeft() + ", top = " + avatarFrame.getTop() + ", right = " + avatarFrame.getRight() + ", bottom = " + avatarFrame.getBottom());
        Rect rect = new Rect((int) minX,
                (int) minY,
                (int) maxX,
                (int) maxY);

        Log.d("TAG", "selectMyFrame: rect left = " + rect.left + ", top = " + rect.top + ", right = " + rect.right + ", bottom = " + rect.bottom);

        if (rect.contains((int) x, (int) y)) {
            //If you select the current view mentioned uppermost layer
            avatarFrame.bringToFront();
            avatarFrame.setSelect(true);
            isShowOptBtn = true;
            LogUtils.e("selected");
        } else {
            avatarFrame.setSelect(false);
            isShowOptBtn = false;
            LogUtils.e("no select");
        }
        Log.d("TAG", "selectMyFrame: avatarSelectImageCount = " + isShowOptBtn);
    }

    class AvatarMyOntouch implements View.OnTouchListener {
        private float baseValue = 0;
        //The original angle
        private float oldRotation;
        //旋转和缩放的中点
        private PointF midP;
        //点中的要进行缩放的点与图片中点的距离
        private float imgLengHalf;
        //保存刚开始按下的点
        private PointF startPoinF = new PointF();

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
                    Log.d("TAG", "ACTION_DOWN: avatarSelectImageCount = " + isShowOptBtn);
                    if (isShowOptBtn) {
                        avatarMatrix = avatarFrame.getMatrix();
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

                            if (isShowOptBtn) {
                                //最后在action_move 执行完前设置好矩阵 设置view的位置
                                avatarMatrix.set(avatarSavedMatrix);
                                avatarMatrix.postScale(-1, 1, midP.x, midP.y);
//                                    avatarMatrix.postScale(scale, scale, midP.x, midP.y);
//                                    avatarMatrix.postRotate(newRotation, midP.x, midP.y);
                                adjustLocation(avatarMatrix, avatarFrame);
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
                    if (isShowOptBtn) {
                        midP = midPoint(avatarFrame.leftTop, avatarFrame.rightBottom);
                        imgLengHalf = spacing(midP, avatarFrame.rightBottom);
                        oldRotation = rotationforTwo(event);
                    }
                    break;

                case MotionEvent.ACTION_MOVE: // touch drag with the ball
                    //如果是双指点中
                    if (event.getPointerCount() == 2) {
                        if (isShowOptBtn) {
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
                        if (isShowOptBtn) {
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
                        if (isShowOptBtn) {
                            //最后在action_move 执行完前设置好矩阵 设置view的位置
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
        Log.d("TAG", "deleteMyFrame: avatarSelectImageCount = " + isShowOptBtn);
        if (isShowOptBtn) {
            avatarContainer.removeView(avatarFrame);
            avatarFrame = null;
            isShowOptBtn = false;
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

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
    private List<AvatarFrameHolder> avatarFrameHolders;
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
        avatarFrameHolders = new ArrayList<>();
        addMyFrame();
    }

    private void initView() {
        frame = (FrameLayout) findViewById(R.id.frame);
        findViewById(R.id.addView).setOnClickListener(this);
    }


    /**
     * ???????????????????????? ???????????????????????????
     */
    private void ajustAvatarFrame() {
        if (avatarSelectImageCount != -1) {
            avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame().getLayout().layoutWidthAndHeight(avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame().getLayout(), new AvatarFrameOutsideLinearLayout.OnLayoutWidth() {
                @Override
                public void layout(int width, int height) {
                    avatarMatrix = avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame().getMatrix();
                    avatarSavedMatrix.set(avatarMatrix);
                    adjustLocation(avatarMatrix, avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame());
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
     * ??????1????????? ??????????????????
     *
     * @param matrix
     * @param avatarFrame
     */
    private void adjustLocation(Matrix matrix, AvatarFrame avatarFrame) {
        //??????????????????????????????????????????????????????f???
        float[] f = new float[9];
        matrix.getValues(f);
        int bWidth = 0;
        int bHeight = 0;

        //??????view?????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Rect rect = new Rect();
            avatarFrame.getLayout().getGlobalVisibleRect(rect);
            bWidth = rect.width();
            bHeight = rect.height();
        } else {
            bWidth = operatedWidth;
            bHeight = operatedHeight;
        }

        //?????????????????????????????????????????????????????????????????????9*9????????????????????????????????? ?????????????????????????????? ????????????
        // ???????????????
        float x1 = f[2];
        float y1 = f[5];
        avatarFrame.leftTop.set(x1, y1);
        // ???????????????
        float x2 = f[0] * bWidth + f[2];
        float y2 = f[3] * bWidth + f[5];
        avatarFrame.rightTop.set(x2, y2);
        // ???????????????
        float x3 = f[1] * bHeight + f[2];
        float y3 = f[4] * bHeight + f[5];
        avatarFrame.leftBottom.set(x3, y3);
        // ???????????????
        float x4 = f[0] * bWidth + f[1] * bHeight + f[2];
        float y4 = f[3] * bWidth + f[4] * bHeight + f[5];
        avatarFrame.rightBottom.set(x4, y4);


        //???????????????????????????????????? ?????? ?????? ??? ??????????????? ??????????????????????????????????????????view
        // ?????????x
        float minX = 0;
        // ?????????x
        float maxX = 0;
        // ?????????y
        float minY = 0;
        // ?????????y
        float maxY = 0;

        minX = Math.min(x4, Math.min(x3, Math.min(x1, x2))) - 30;
        maxX = Math.max(x4, Math.max(x3, Math.max(x1, x2))) + 30;
        minY = Math.min(y4, Math.min(y3, Math.min(y1, y2))) - 30;
        maxY = Math.max(y4, Math.max(y3, Math.max(y1, y2))) + 30;

        avatarFrameHolders.get(avatarSelectImageCount).getState().setLeft(minX);
        avatarFrameHolders.get(avatarSelectImageCount).getState().setTop(minY);
        avatarFrameHolders.get(avatarSelectImageCount).getState().setRight(maxX);
        avatarFrameHolders.get(avatarSelectImageCount).getState().setBottom(maxY);

        //????????????view?????????????????????
        avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame().setMatrix(matrix);
    }

    private void addMyFrame() {
        //Each additional view Now  set all view is not selected
        for (int i = (avatarFrameHolders.size() - 1); i >= 0; i--) {
            AvatarFrame avatarFrame = avatarFrameHolders.get(i).getAvatarFrame();
            if (avatarFrame.isSelect()) {
                avatarFrame.setSelect(false);
                break;
            }
        }

        //new a view
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

        AvatarFrameHolder avatarFrameHolder = new AvatarFrameHolder();
        avatarFrameHolder.setAvatarFrame(avatarFrame);
        avatarFrameHolder.setState(avatarFrameState);
        avatarFrameHolders.add(avatarFrameHolder);

        avatarFrame.setOnTouchListener(new AddWordMyOntouch());
        avatarSelectImageCount = avatarFrameHolders.size() - 1;
    }

    private void selectMyFrame(float x, float y) {
        //Select the option to cancel all back to only one click is selected
        for (int i = (avatarFrameHolders.size() - 1); i >= 0; i--) {
            AvatarFrameHolder avatarFrameHolder = avatarFrameHolders.get(i);
            if (avatarFrameHolder.getAvatarFrame().isSelect()) {
                avatarFrameHolder.getAvatarFrame().setSelect(false);
                break;
            }
        }

        for (int i = (avatarFrameHolders.size() - 1); i >= 0; i--) {
            AvatarFrameHolder avatarFrameHolder = avatarFrameHolders.get(i);
            //Create a rectangular area here getLeft getTop etc. mean the current view of the leftmost
            // uppermost and lowermost rightmost only to click inside the region is selected
            Rect rect = new Rect((int) avatarFrameHolder.getState().getLeft(),
                    (int) avatarFrameHolder.getState().getTop(),
                    (int) avatarFrameHolder.getState().getRight(),
                    (int) avatarFrameHolder.getState().getBottom());

            if (rect.contains((int) x, (int) y)) {
                //If you select the current view mentioned uppermost layer
                avatarFrameHolder.getAvatarFrame().bringToFront();
                avatarFrameHolder.getAvatarFrame().setSelect(true);
                //Which record is selected
                avatarSelectImageCount = i;
                LogUtils.e("selected");
                break;
            }
            avatarSelectImageCount = -1;
            LogUtils.e("no select");
        }
    }

    class AddWordMyOntouch implements View.OnTouchListener {
        private float baseValue = 0;
        //The original angle
        private float oldRotation;
        //????????????????????????
        private PointF midP;
        //??????????????????????????????????????????????????????
        private float imgLengHalf;
        //???????????????????????????
        private PointF startPoinF = new PointF();

        private int NONE = 0; // ???
        private int DRAG = 1; // ??????
        private int ZOOM = 2; // ??????
        private int DOUBLE_ZOOM = 3;
        private int MIRROR = 4;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eventaction = event.getAction();
            float event_x = (int) event.getRawX();
            float event_y = (int) event.getRawY() - StatusBarHeightUtil.getStatusBarHeight(context);

            //????????????????????????????????? ?????????????????????????????????100 * 100 ??????????????? ????????????????????????????????????
            int tempInt = 50;
            int addint = 50;

            switch (eventaction & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: // touch down so check if the

                    baseValue = 0;

                    startPoinF.set(event_x, event_y);// ??????????????????????????????

                    //????????????????????????????????????view ???????????????????????????
                    selectMyFrame(event_x, event_y);

                    //???????????????????????????view
                    if (avatarSelectImageCount != -1) {
                        avatarFrame = avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame();
                        avatarMatrix = avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame().getMatrix();
                        avatarSavedMatrix.set(avatarMatrix);
                        operateMode = DRAG;

                        //???????????????????????????????????????
                        Rect moveRect = new Rect((int) avatarFrame.rightBottom.x - tempInt,
                                (int) avatarFrame.rightBottom.y - tempInt, (int) avatarFrame.rightBottom.x + addint,
                                (int) avatarFrame.rightBottom.y + addint);
                        //???????????????????????????
                        Rect mirrorRect = new Rect((int) avatarFrame.leftTop.x - tempInt,
                                (int) avatarFrame.leftTop.y - tempInt, (int) avatarFrame.leftTop.x + addint,
                                (int) avatarFrame.leftTop.y + addint);
                        //???????????????????????????
                        Rect deleteRect = new Rect((int) avatarFrame.rightTop.x - tempInt,
                                (int) avatarFrame.rightTop.y - tempInt, (int) avatarFrame.rightTop.x + addint,
                                (int) avatarFrame.rightTop.y + addint);


                        //?????????????????????
                        if (moveRect.contains((int) event_x, (int) event_y)) {
                            LogUtils.e("???????????????????????????");
                            // ???????????????
                            midP = midPoint(avatarFrame.leftTop, avatarFrame.rightBottom);
                            imgLengHalf = spacing(midP, avatarFrame.rightBottom);
                            oldRotation = rotation(midP, startPoinF);
                            operateMode = ZOOM;
                        } else if (mirrorRect.contains((int) event_x, (int) event_y)) {
                            // ???????????????
                            LogUtils.e("?????????????????????");
                            midP = midPoint(avatarFrame.leftTop, avatarFrame.rightBottom);
                            imgLengHalf = spacing(midP, avatarFrame.rightBottom);
                            oldRotation = rotation(midP, startPoinF);
                            operateMode = MIRROR;

                            if (operateMode != NONE) {
                                if (avatarSelectImageCount != -1) {
                                    //?????????action_move ??????????????????????????? ??????view?????????
                                    avatarFrame = avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame();
                                    avatarMatrix.set(avatarSavedMatrix);
                                    avatarMatrix.postScale(-1, 1, midP.x, midP.y);
//                                    avatarMatrix.postScale(scale, scale, midP.x, midP.y);
//                                    avatarMatrix.postRotate(newRotation, midP.x, midP.y);
                                    adjustLocation(avatarMatrix, avatarFrame);
                                }
                            }

                        } else if (deleteRect.contains((int) event_x, (int) event_y)) {
                            // ???????????????
                            LogUtils.e("???????????????");
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
                    //?????????????????????
                    if (event.getPointerCount() == 2) {
                        if (avatarSelectImageCount != -1) {
                            operateMode = DOUBLE_ZOOM;
                            float x = event.getX(0) - event.getX(1);
                            float y = event.getY(0) - event.getY(1);
                            float value = (float) Math.sqrt(x * x + y * y);// ?????????????????????

                            //???????????????
                            float newRotation = rotationforTwo(event) - oldRotation;
                            if (baseValue == 0) {
                                baseValue = value;
                            } else {
                                //?????????????????????????????? ??????????????????????????????????????????
                                if (value - baseValue >= 15 || value - baseValue <= -15) {
                                    float scale = value / baseValue;// ?????????????????????????????????????????????????????????????????????????????????????????????
                                    avatarMatrix.set(avatarSavedMatrix);
                                    avatarMatrix.postScale(scale, scale, midP.x, midP.y);
                                    avatarMatrix.postRotate(newRotation, midP.x, midP.y);
                                }
                            }
                        }
                    } else if (event.getPointerCount() == 1) {
                        //????????????
                        if (avatarSelectImageCount != -1) {
                            if (operateMode == DRAG) {
                                if (event_x < MyApplication.getInstance().getScreenWidth() - 50 && event_x > 50
                                        && event_y > 100
                                        && event_y < MyApplication.getInstance().getScreenHeight() - 100) {
                                    avatarMatrix.set(avatarSavedMatrix);
                                    // ?????????????????????
                                    float translateX = event_x - startPoinF.x;
                                    float translateY = event_y - startPoinF.y;
                                    avatarMatrix.postTranslate(translateX, translateY);
                                }
                            } else if (operateMode == ZOOM) {
                                //??????????????????????????????
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
                            //?????????action_move ??????????????????????????? ??????view?????????
                            avatarFrame = avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame();
                            adjustLocation(avatarMatrix, avatarFrame);
                        }
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP: //????????????????????????????????????????????????????????????????????????
                    //???????????????
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
            frame.removeView(avatarFrameHolders.get(avatarSelectImageCount).getAvatarFrame());
            avatarFrameHolders.remove(avatarSelectImageCount);
            avatarSelectImageCount = -1;
        }
    }

    private float rotationforTwo(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    // ????????????????????????
    private float spacing(PointF p1, PointF p2) {
        float x = p1.x - p2.x;
        float y = p1.y - p2.y;
        return (float) Math.sqrt(x * x + y * y);
    }

    // ????????????????????????
    private PointF midPoint(PointF p1, PointF p2) {
        PointF point = new PointF();
        float x = p1.x + p2.x;
        float y = p1.y + p2.y;
        point.set(x / 2, y / 2);
        return point;
    }

    // ??????
    private float rotation(PointF p1, PointF p2) {
        double delta_x = (p1.x - p2.x);
        double delta_y = (p1.y - p2.y);
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

}

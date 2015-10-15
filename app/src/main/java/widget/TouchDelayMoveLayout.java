package widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mars on 2015/10/13.
 */
public class TouchDelayMoveLayout extends FrameLayout {

    private boolean mDownInChildren;
    boolean mTouchModel = true;
    private long mStartTime;
    List<MoveInfo> mMoveInfos = new ArrayList<MoveInfo>();
    private List<Point> mPoints = new ArrayList<Point>();
    final int mTouchSlop;
    boolean isDragging;
    float mLastMotionX;
    float mLastMotionY;
    static final long START_MOVE_DELAY = 80;
    static final long FOLLOW_MOVE_DELAY = 120;
    private boolean mPressed;
    OverTranslateInterpolator mInterpolator = new OverTranslateInterpolator();
    Paint mMenuBgPaint = new Paint();
    boolean mSwitchAnimatorWorking = false;
    private AnimatorListenerAdapter mSwitchAnimatorListener;

    public TouchDelayMoveLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMenuBgPaint.setColor(Color.DKGRAY);
        mSwitchAnimatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mSwitchAnimatorWorking = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mSwitchAnimatorWorking = false;
            }
        };
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            FrameLayout.LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            layoutParams.gravity = Gravity.END | Gravity.BOTTOM;
            layoutParams.bottomMargin = 60;
            layoutParams.rightMargin = 60;
            child.setLayoutParams(layoutParams);
        }
        super.onLayout(changed, left, top, right, bottom);
        for (int i = 0; i < getChildCount(); i++) {
            MoveInfo moveInfo = mMoveInfos.get(i);
            View child = getChildAt(i);
            moveInfo.pointIndex = 0;
            moveInfo.centerX = (child.getLeft() + child.getRight()) / 2;
            moveInfo.centerY = (child.getTop() + child.getBottom()) / 2;
        }
    }

    void moveChildrenToMenuPosition(){
        int radius = Math.min(getWidth(),getHeight()) / 2;
        int maxChildSize = -1;
        for (int i = 0; i < getChildCount(); i++){
            View child = getChildAt(i);
            int curSize =Math.max(child.getWidth(),child.getHeight());
            if (curSize > maxChildSize){
                maxChildSize = curSize;
            }
        }
        radius -= maxChildSize;
        double curDegree;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        double degreeSize = 2 * Math.PI / getChildCount();
        for (int i = 0; i < getChildCount(); i++){
            View child = getChildAt(i);
            curDegree = i * degreeSize;
            float cx = (float) (radius * Math.cos(curDegree) + centerX);
            float cy = (float) (radius * Math.sin(curDegree) + centerY);
            MoveInfo moveInfo = mMoveInfos.get(i);
            child.animate().setInterpolator(mInterpolator).setDuration(600)
                    .translationX(cx - moveInfo.centerX).translationY(cy - moveInfo.centerY);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMoveInfos.clear();
        for (int i = 0; i < getChildCount(); i++) {
            mMoveInfos.add(new MoveInfo());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mTouchModel){
            return super.onInterceptTouchEvent(ev);
        }
        return mTouchModel;
    }

    boolean isInChildren(MotionEvent event) {
        Rect rect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains((int) event.getX(), (int) event.getY())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressed = true;
                if (isInChildren(event)) {
                    System.out.println("down in children");
                    mStartTime = -1;
                    mDownInChildren = true;
                } else {
                    mDownInChildren = false;
                }
                isDragging = false;
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (isInChildren(event) && mDownInChildren) {
                    switchToMenuModel();
                }
                mPressed = false;
                if (allChildrenStop()){
                    letChildrenBack();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isDragging){
                    if (Math.abs(event.getX() - mLastMotionX) >= mTouchSlop ||
                            Math.abs(event.getY() - mLastMotionY) >= mTouchSlop) {
                        isDragging = true;
                    }
                }
                if (isDragging && mDownInChildren) {
                    mLastMotionX = event.getX();
                    mLastMotionY = event.getY();
                    mPoints.add(new Point(mLastMotionX, mLastMotionY));
                    if (-1 == mStartTime) {
                        mStartTime = System.currentTimeMillis();
                        post(mConsumePoints);
                    }
                }
                break;
        }
        if (mTouchModel) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void switchToMenuModel() {
        mTouchModel = false;
        ObjectAnimator menuBgAnimator = ObjectAnimator.ofInt(this, "menuBgAlpha", 0, 255)
                .setDuration(600);
        menuBgAnimator.addListener(mSwitchAnimatorListener);
        menuBgAnimator.start();
    }

    public void setMenuBgAlpha(int alpha){
        mMenuBgPaint.setAlpha(alpha);
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!mTouchModel || mSwitchAnimatorWorking){
            canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mMenuBgPaint);
        }
        super.dispatchDraw(canvas);
    }

    void moveChildFollowFinger(int i){
        MoveInfo moveInfo = mMoveInfos.get(i);
        if (moveInfo.pointIndex < mPoints.size()) {
            Point point = mPoints.get(moveInfo.pointIndex);
            View child = getChildAt(i);
//                    int offsetLeftAndRight = (int) (point.x) - child.getLeft();
//                    int offsetTopAndBottom = (int) (point.y) - child.getTop();
            int translationX = (int) (point.x - moveInfo.centerX);
            int translationY = (int) (point.y - moveInfo.centerY);
//                    String prints = String.format("x:%f, y:%f, layoutLeft:%d, layoutTop:%d, offsetH:%d, offsetV:%d",
//                            point.x,point.y,moveInfo.layoutLeft,moveInfo.layoutTop,offsetLeftAndRight,offsetTopAndBottom);
//                    child.offsetLeftAndRight(offsetLeftAndRight);
//                    child.offsetTopAndBottom(offsetTopAndBottom);
            child.setTranslationX(translationX);
            child.setTranslationY(translationY);
            moveInfo.pointIndex++;
        }
    }
    private void moveChildrenFollowFinger() {
        long curTime = System.currentTimeMillis();
        if (curTime - mStartTime < START_MOVE_DELAY){
            return;
        }
        for (int i = getChildCount() - 1; i > -1; i--) {
            if (i == getChildCount() - 1){
                moveChildFollowFinger(i);
            }else {
                if (curTime - mStartTime >= (getChildCount() - 1 - i) * FOLLOW_MOVE_DELAY + START_MOVE_DELAY) {
                    moveChildFollowFinger(i);
                }
            }
        }
        if (allChildrenStop()) {
            mPoints.clear();
            resetMoveInfo();
            mStartTime = -1;
            if (!mPressed){
                letChildrenBack();
            }
        }
    }
    boolean allChildrenStop(){
        return mMoveInfos.get(0).pointIndex == mPoints.size();
    }
    public void letChildrenBack(){
        if (mTouchModel){
            for (int i = getChildCount() - 1; i > -1; i--){
                View child = getChildAt(i);
                child.animate().translationX(0).translationY(0)
                        .setDuration(600).setStartDelay((getChildCount() - 1 - i) * FOLLOW_MOVE_DELAY + START_MOVE_DELAY)
                        .setInterpolator(mInterpolator);
            }
        }else {
            moveChildrenToMenuPosition();
        }
    }
    public void switchToTouchModel(){
        ObjectAnimator menuBgAnimator = ObjectAnimator.ofInt(this, "menuBgAlpha", 255, 0)
                .setDuration(600);
        menuBgAnimator.addListener(mSwitchAnimatorListener);
        menuBgAnimator.start();
        mTouchModel = true;
    }
    class OverTranslateInterpolator implements TimeInterpolator{

        @Override
        public float getInterpolation(float input) {
            if (input <= 0.5f){
                return input * 2f;
            }else if (input > 0.5f && input <= 0.75f){
                return (input - 0.5f) * 0.2f + 1f;
            }else {
                return 1.05f - 0.2f * (input - 0.75f);
            }
        }
    }
    private void resetMoveInfo() {
        for (int i = 0; i < getChildCount(); i++) {
            MoveInfo moveInfo = mMoveInfos.get(i);
            moveInfo.pointIndex = 0;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mConsumePoints);
        resetMoveInfo();
    }

    Runnable mConsumePoints = new Runnable() {
        @Override
        public void run() {
            moveChildrenFollowFinger();
            if (mPoints.size() > 0) {
                post(this);
            }
        }
    };

    class Point {
        float x;
        float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    class MoveInfo {
        int pointIndex;
        int centerX;
        int centerY;
    }
}

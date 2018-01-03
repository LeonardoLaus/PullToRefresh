package leon.android.pulltorefresh;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;

import static leon.android.pulltorefresh.State.STATE_COMPLETE;
import static leon.android.pulltorefresh.State.STATE_IDLE;
import static leon.android.pulltorefresh.State.STATE_PULL_TO_REFRESH;
import static leon.android.pulltorefresh.State.STATE_REFRESHING;
import static leon.android.pulltorefresh.State.STATE_RELEASE_TO_REFRESH;

/**
 * Created by LHEE on 2017/6/25.
 */

public class NestedPullRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild, IPullToRefresh {

    private static final String TAG = "NestedPullRefreshLayout";
    private static final int DEFAULT_SCROLL_DURATION = 1000;
    private static final float DEFAULT_OFFSET_RATIO = 3.0f;
    private static final int OVER_SCROLL_MIN_VX = 3000;

    @State
    private int mState;

    private IRefreshHeader mRefreshHeader;
    private View mTarget;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private int mLastScrollY = 0;
    private float mCurrentOffset;
    private float mTouchDistance;
    private float mMotionDownY;
    private float mLastMotionY;

    private int mTouchSlop;
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;

    private float mTotalUnconsumed;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;

    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];

    private OnChildScrollCallback<NestedPullRefreshLayout> mOnChildScrollCallback;
    private OnRefreshListener mOnRefreshListener;

    private OverScrollHandler mHandler = new OverScrollHandler(this);
    private ValueAnimator mOverScrollAnimator;

    private boolean mOverScrollEnable;
    private boolean mOverScrollTopShow;

    private MotionEvent mLastMoveEvent;
    private boolean mNestedScrollInProgress;

    public NestedPullRefreshLayout(Context context) {
        this(context, null);
    }

    public NestedPullRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedPullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        mScroller = new Scroller(context);
        mOverScrollAnimator = ValueAnimator.ofInt(0, 0);
        mOverScrollAnimator.setInterpolator(new OverScrollInterpolator());
        mOverScrollAnimator.addUpdateListener(mOverScrollAnimatorListener);
        mOverScrollEnable = true;
        mOverScrollTopShow = true;
        setState(STATE_IDLE);
        onStateChanged(mState);
        setRefreshHeader(new DefaultRefreshHeader(context));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0)
            return;
        if (mTarget == null)
            ensureTarget();
        if (mTarget == null)
            return;
        final int offset = (int) mCurrentOffset;
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        //layout offset
        child.layout(childLeft, childTop + offset, childWidth, childHeight + offset);
        if (mRefreshHeader != null && mRefreshHeader.getView() != null) {
            if (mOverScrollAnimator.isRunning() && !mOverScrollTopShow)
                return;
            final View header = mRefreshHeader.getView();
            final int headerWidth = header.getMeasuredWidth();
            final int headerHeight = header.getMeasuredHeight();
            //layout offset
            header.layout(childLeft, childTop + offset - headerHeight,
                    headerWidth, childTop + offset);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null)
            ensureTarget();
        if (mTarget == null)
            return;
        mTarget.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        if (mRefreshHeader != null && mRefreshHeader.getView() != null) {
            final View header = mRefreshHeader.getView();
            measureChild(header, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int offset = mLastScrollY - mScroller.getCurrY();
            mLastScrollY = mScroller.getCurrY();
            Log.v(TAG, "[computeScroll]offset before=" + mCurrentOffset + ", currentY=" + mScroller.getCurrY());
            mCurrentOffset += offset;
            mTotalUnconsumed = mCurrentOffset * DEFAULT_OFFSET_RATIO;
            Log.d(TAG, "[computeScroll]" + mCurrentOffset + ", total=" + mTotalUnconsumed);
            offsetViewTopAndBottom(offset);
            ensureState();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG, "[dispatchTouchEvent] action=" + MotionEventCompat.getActionMasked(ev));
        dealFling(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void dealFling(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                reset();
                mLastMotionY = mMotionDownY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                mTouchDistance = ev.getY() - mMotionDownY;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                float velocityY = mVelocityTracker.getYVelocity();
                float velocityX = mVelocityTracker.getXVelocity();
                if (Math.abs(velocityX) > mMinimumFlingVelocity
                        || Math.abs(velocityY) > mMinimumFlingVelocity) {
                    onFling(velocityX, velocityY);
                }
                releaseVelocity();
                break;
            case MotionEvent.ACTION_CANCEL:
                releaseVelocity();
                break;
        }
    }

    private void releaseVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void onFling(final float velocityX, final float velocityY) {
        // velocity < 0 手势向上
        Log.d(TAG, "[onFling] velocityX= " + velocityX + ", velocityY=" + velocityY);
        if (mNestedScrollInProgress)
            return;
        if (Math.abs(mTouchDistance) > mTouchSlop) {
            if (!mOverScrollEnable)
                return;
            if (mCurrentOffset != 0)
                return;
            if (velocityY > 0 && !canChildScrollUp())
                return;
            if (velocityY < 0 && !canChildScrollDown())
                return;
            if (Math.abs(velocityY) > OVER_SCROLL_MIN_VX) {
                Log.e(TAG, "OverScroll Fling");
                mHandler.obtainMessage(OverScrollHandler.MSG_START_COMPUTE_SCROLL, -velocityY).sendToTarget();
            }
        }
    }

    /*
      解决未实现NestScrollingChild的Target的下拉刷新
      两种方案：
      A. 重写dispatchTouchEvent
      B. 重写onInterceptTouchEvent、onTouchEvent、requestDisallowInterceptTouchEvent
     */

    /**    A.START:{   ***/
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        final int action = MotionEventCompat.getActionMasked(ev);
//        Log.e(TAG, "[dispatchTouchEvent] action=" + action);
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                reset();
//                mLastMotionY = mMotionDownY = ev.getY();
//                if (mState != STATE_REFRESHING)
//                    setState(STATE_IDLE);
//                break;
//            case MotionEvent.ACTION_UP:
//                mTouchDistance = ev.getY() - mMotionDownY;
//            case MotionEvent.ACTION_CANCEL:
//                if (mNestedScrollInProgress)
//                    return super.dispatchTouchEvent(ev);
//                mLastScrollY = 0;
//                if (mCurrentOffset > 0) {
//                    finishDragTarget();
//                    return true;
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (mNestedScrollInProgress)
//                    return super.dispatchTouchEvent(ev);
//                mLastMoveEvent = ev;
//                float distance = ev.getY() - mMotionDownY;
//                float dy = ev.getY() - mLastMotionY;
//                mLastMotionY = ev.getY();
//                Log.d(TAG, "distance=" + distance + ",dy=" + dy + ", current=" + mCurrentOffset);
//                //上滑
//                if (dy < 0 && mCurrentOffset <= 0) {
//                    mCurrentOffset = mTotalUnconsumed = 0;
//                    return super.dispatchTouchEvent(ev);
//                }
//                //下滑
//                if (dy > 0 && canChildScrollUp())
//                    return super.dispatchTouchEvent(ev);
//                Log.e(TAG, "not return");
//                float offset = dy / DEFAULT_OFFSET_RATIO;
//                if (!mScroller.isFinished())
//                    mScroller.forceFinished(true);
//                if (mOverScrollAnimator.isRunning())
//                    mOverScrollAnimator.end();
//                if (offset < 0 && mCurrentOffset > 0) {
//                    if (Math.abs(offset) > mCurrentOffset) {
//                        sendCancelEvent();
//                        offsetViewTopAndBottom((int) mCurrentOffset);
//                        mCurrentOffset = 0;
//                        ensureState();
//                        sendDownEvent();
//                        if (!mNestedScrollInProgress) {
//                            super.dispatchTouchEvent(ev);
//                        }
//                        return true;
//                    }
//                }
//                mCurrentOffset += offset;
//                offsetViewTopAndBottom((int) offset);
//                ensureState();
//                sendCancelEvent();
//                if (!mNestedScrollInProgress) {
//                    super.dispatchTouchEvent(ev);
//                }
//                return true;
//        }
//        return super.dispatchTouchEvent(ev);
//    }
    /**    A.END:}   ***/

    /**
     * B.START:{
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e(TAG, "[onInterceptTouchEvent] action=" + ev.getAction());
        if (mRefreshHeader == null || mNestedScrollInProgress) {
            return super.onInterceptTouchEvent(ev);
        }
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mState != STATE_REFRESHING) {
                    setState(STATE_IDLE);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = ev.getY() - mMotionDownY;
                float dy = ev.getY() - mLastMotionY;
                mLastMotionY = ev.getY();
                if (mScroller.isFinished() && mState != STATE_REFRESHING) {
                    mCurrentOffset = mTotalUnconsumed = 0;
                }
                Log.d(TAG, "distance=" + distance + ",dy=" + dy + ", offset=" + mCurrentOffset);
                //上滑
                if (dy < 0 && mCurrentOffset <= 0) {
                    return super.onInterceptTouchEvent(ev);
                }
                //下滑
                if (dy > 0 && canChildScrollUp()) {
                    return super.onInterceptTouchEvent(ev);
                }
                return true;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "[onTouchEvent] action=" + event.getAction());
        if (mRefreshHeader == null) {
            return super.onTouchEvent(event);
        }
        if (mNestedScrollInProgress)
            return super.onTouchEvent(event);
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = event;
                float dy = event.getY() - mLastMotionY;
                mLastMotionY = event.getY();
                if ((dy < 0 && mCurrentOffset <= 0)//上滑
                        || (dy > 0 && canChildScrollUp())) {//下滑
                    return super.onTouchEvent(event);
                }
                float offset = dy / DEFAULT_OFFSET_RATIO;
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                if (mOverScrollAnimator.isRunning()) {
                    mOverScrollAnimator.end();
                }
                if (offset < 0 && mCurrentOffset > 0) {
                    if (Math.abs(offset) > mCurrentOffset) {
                        offsetViewTopAndBottom((int) mCurrentOffset);
                        mCurrentOffset = 0;
                        ensureState();
                        sendDownEvent();
                        return true;
                    }
                }
                mCurrentOffset += offset;
                offsetViewTopAndBottom((int) offset);
                ensureState();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastScrollY = 0;
                if (mCurrentOffset > 0) {
                    finishDragTarget();
                    return true;
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                && mTarget instanceof AbsListView)
                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
            // Nope.
            Log.d(TAG, "request Nope");
        } else {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    /**
     * B.END:}
     */

    //发送cancel事件解决selection问题
    private void sendCancelEvent() {
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    /**
     * NestedScrollingParent
     *
     * @param child
     * @param target
     * @param nestedScrollAxes
     * @return
     */
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled()
                && mRefreshHeader != null && mRefreshHeader.getView() != null
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mNestedScrollInProgress = true;
        if (mState != STATE_REFRESHING) {
            setState(STATE_IDLE);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.i(TAG, "onNestedPreScroll: " + dx + ", " + dy);
        Log.i(TAG, "consumed=" + consumed[0] + ", " + consumed[1]);
        Log.i(TAG, "onPreScroll=" + mTotalUnconsumed + ",dy=" + dy);
        //上滑时的处理
        if (dy > 0 && mTotalUnconsumed > 0) {
            if (!mScroller.isFinished())
                mScroller.forceFinished(true);
            if (mOverScrollAnimator.isRunning())
                mOverScrollAnimator.end();
            if (dy > mTotalUnconsumed) {
                consumed[1] = (int) (dy - mTotalUnconsumed);
                mTotalUnconsumed = 0;
            } else {
                consumed[1] = dy;
                mTotalUnconsumed -= dy;
            }
            startDragTarget(mTotalUnconsumed);
            ensureState();
        }
        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        Log.e(TAG, "dxConsumed=" + dxConsumed + ",dyConsumed=" + dyConsumed
                + ",dxUnconsumed=" + dxUnconsumed + ", dyUnconsumed=" + dyUnconsumed);
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);
        //下滑时处理
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !canChildScrollUp()) {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            if (mOverScrollAnimator.isRunning()) {
                mOverScrollAnimator.end();
            }
            Log.e(TAG, "startDrag " + mTotalUnconsumed);
            mTotalUnconsumed += (-dy);
            startDragTarget(mTotalUnconsumed);
            ensureState();
        }

    }

    @Override
    public void onStopNestedScroll(View target) {
        Log.d(TAG, "[onStopNestedScroll]");
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        mLastScrollY = 0;
        if (mCurrentOffset > 0) {
            finishDragTarget();
        }
        stopNestedScroll();
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        //处理Fling事件 velocityY > 0 向上快速滑动
        Log.d(TAG, "[onNestedPreFling]" + "velocityX=" + velocityX + ",velocityY=" + velocityY);
        Log.d(TAG, "mTouchDistance=" + mTouchDistance + " && slop=" + mTouchSlop);
        Log.d(TAG, "canChildScrollUp=" + canChildScrollUp());
        Log.d(TAG, "canChildScrollDown=" + canChildScrollDown());
        if (Math.abs(mTouchDistance) > mTouchSlop) {
            if (!mOverScrollEnable)
                return dispatchNestedPreFling(velocityX, velocityY);
            if (velocityY < 0 && !canChildScrollUp())
                return dispatchNestedPreFling(velocityX, velocityY);
            if (velocityY > 0 && !canChildScrollDown())
                return dispatchNestedPreFling(velocityX, velocityY);
            if (Math.abs(velocityY) > OVER_SCROLL_MIN_VX)
                mHandler.obtainMessage(OverScrollHandler.MSG_START_COMPUTE_SCROLL, velocityY).sendToTarget();
        }
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    /**
     * NestedScrollingChild
     *
     * @param enabled
     */
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                        int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed,
                                           int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(getWindowToken());
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        View headerView = null;
        if (mRefreshHeader != null) {
            headerView = mRefreshHeader.getView();
        }
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(headerView)) {
                    mTarget = child;
                    break;
                }
            }
        }
        if (mTarget != null) {
            mTarget.setOverScrollMode(mOverScrollEnable ? OVER_SCROLL_NEVER : OVER_SCROLL_ALWAYS);
        }
    }

    /**
     * Target 是否到顶部, 手势向下
     *
     * @return
     */
    public boolean canChildScrollUp() {
        if (mOnChildScrollCallback != null) {
            return mOnChildScrollCallback.canChildScrollUp(this, mTarget);
        }
        if (mTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTarget, -1);
        }
        return ViewCompat.canScrollVertically(mTarget, -1);
    }

    /**
     * Target 是否到底部，手势向上
     *
     * @return
     */
    public boolean canChildScrollDown() {
        if (mOnChildScrollCallback != null) {
            return mOnChildScrollCallback.canChildScrollDown(this, mTarget);
        }
        if (mTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTarget, 1);
        } else if (mTarget instanceof WebView) {
            final WebView webView = (WebView) mTarget;
            return ViewCompat.canScrollVertically(mTarget, 1)
                    || (webView.getContentHeight() * webView.getScaleY() != webView.getHeight() + webView.getScrollY());
        } else if (mTarget instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) mTarget;
            View childView = scrollView.getChildAt(0);
            if (childView != null) {
                return ViewCompat.canScrollVertically(childView, 1)
                        || (scrollView.getScrollY() != childView.getHeight() - scrollView.getHeight());
            }
        }
        return ViewCompat.canScrollVertically(mTarget, 1);
    }

    private void startDragTarget(float overScrollTop) {
        mCurrentOffset = overScrollTop / DEFAULT_OFFSET_RATIO;
        final int offset = (int) (mCurrentOffset - mTarget.getTop());
        offsetViewTopAndBottom(offset);
    }

    private void offsetViewTopAndBottom(int offset) {
        mRefreshHeader.getView().bringToFront();
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            ViewCompat.offsetTopAndBottom(child, offset);
        }
        requestLayout();
        Log.e(TAG, "current offset=" + mCurrentOffset
                + ", total=" + mTotalUnconsumed);
        mRefreshHeader.onPositionChanged(mCurrentOffset / (float) getThresholdDistance(), mState);
    }

    private void overScrollOffsetTopAndBottom(final int value) {
        final int offset = (int) (value - mCurrentOffset);
        mRefreshHeader.getView().bringToFront();
        if (mOverScrollTopShow) {
            ViewCompat.offsetTopAndBottom(mRefreshHeader.getView(), offset);
        }
        ViewCompat.offsetTopAndBottom(mTarget, offset);
        mCurrentOffset = value;
        mTotalUnconsumed = mCurrentOffset * DEFAULT_OFFSET_RATIO;
        mRefreshHeader.onPositionChanged(mCurrentOffset / (float) getThresholdDistance(), mState);
    }

    private void finishDragTarget() {
        final int threshold = getThresholdDistance();
        if (mState == STATE_REFRESHING) {
            if (mCurrentOffset >= threshold) {
                mScroller.startScroll(0, 0, 0, (int) (mCurrentOffset - threshold), currentScrollDuration());
            } else {
                mScroller.startScroll(0, 0, 0, (int) mCurrentOffset, currentScrollDuration());
            }
        } else if (mState == STATE_RELEASE_TO_REFRESH) {
            setState(STATE_REFRESHING);
            mScroller.startScroll(0, 0, 0, (int) (mCurrentOffset - threshold), currentScrollDuration());
        } else {
            mScroller.startScroll(0, 0, 0, (int) mCurrentOffset, currentScrollDuration());
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private int getThresholdDistance() {
        return mRefreshHeader.getRefreshThreshold();
    }

    private int currentScrollDuration() {
        int duration;
        final int threshold = getThresholdDistance();
        float offset = mCurrentOffset;
        if (offset > threshold) {
            offset -= threshold;
        }
        if (offset > threshold) {
            return DEFAULT_SCROLL_DURATION;
        }
        duration = (int) (offset / threshold * DEFAULT_SCROLL_DURATION);
        return duration;
    }

    private void reset() {
        mHandler.removeCallbacksAndMessages(getWindowToken());
    }

    private void overScroll(float velocityY, long delay, boolean scrollToTop) {
        final int headerThreshold = getThresholdDistance();
        int overHeight = (int) Math.abs(velocityY / delay / 2);
        final int realOverHeight = overHeight > headerThreshold ? headerThreshold : overHeight;
        final int duration = realOverHeight < 50 ? 300 : (int) (0.8f * realOverHeight + 200);
        Log.e(TAG, "[overScroll] " + realOverHeight + ", duration=" + duration);
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mOverScrollAnimator.setIntValues(0, scrollToTop ? realOverHeight : -realOverHeight);
        mOverScrollAnimator.setDuration(duration);
        mOverScrollAnimator.start();
    }

    private void ensureState() {
        if (mState == STATE_REFRESHING || mState == STATE_COMPLETE) {
            return;
        }
        final int threshold = getThresholdDistance();
        if (mCurrentOffset > threshold) {
            setState(STATE_RELEASE_TO_REFRESH);
        } else {
            setState(STATE_PULL_TO_REFRESH);
        }
    }

    protected void setState(@State int state) {
        if (mState != state) {
            mState = state;
            onStateChanged(mState);
        }
    }

    private void onStateChanged(@State int state) {
        if (mState == STATE_REFRESHING && mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
        if (mRefreshHeader == null || mRefreshHeader.getStateChangedListener() == null) {
            return;
        }
        final OnStateChangedListener listener = mRefreshHeader.getStateChangedListener();
        listener.onStateChanged(state);
    }

    @Override
    public void setRefreshHeader(@NonNull IRefreshHeader refreshHeader) {
        if (mRefreshHeader != null) {
            removeView(mRefreshHeader.getView());
        }
        if (refreshHeader.getView() == null) {
            throw new NullPointerException("header view should not be Null");
        }
        mRefreshHeader = refreshHeader;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        addView(mRefreshHeader.getView(), 0, params);
        ensureTarget();
    }

    @Override
    public void refreshComplete() {
        setState(STATE_COMPLETE);
        mLastScrollY = 0;
        if (mCurrentOffset <= 0) {
            return;
        }
        mScroller.startScroll(0, 0, 0, (int) mCurrentOffset, DEFAULT_SCROLL_DURATION);
        postInvalidate();
    }

    public void autoRefresh() {
        if (mState != STATE_REFRESHING) {
            setState(STATE_IDLE);
            mLastScrollY = 0;
            mScroller.startScroll(0, 0, 0, -getThresholdDistance(), DEFAULT_SCROLL_DURATION / 2);
            postInvalidate();
            setState(STATE_REFRESHING);
        }
    }

    @Override
    public void setOnChildScrollCallback(OnChildScrollCallback callback) {
        this.mOnChildScrollCallback = callback;
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mOnRefreshListener = listener;
    }

    public void setOverScrollEnable(boolean overScrollEnable) {
        this.mOverScrollEnable = overScrollEnable;
        if (mTarget != null) {
            mTarget.setOverScrollMode(mOverScrollEnable ? OVER_SCROLL_NEVER : OVER_SCROLL_ALWAYS);
        }
    }

    public void setOverScrollTopShow(boolean overScrollTopShow) {
        this.mOverScrollTopShow = overScrollTopShow;
    }

    private ValueAnimator.AnimatorUpdateListener mOverScrollAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final int val = (int) animation.getAnimatedValue();
            overScrollOffsetTopAndBottom(val);
        }
    };

    static class OverScrollHandler extends Handler {
        static final int MSG_START_COMPUTE_SCROLL = 0;
        static final int MSG_CONTINUE_COMPUTE_SCROLL = 1;
        static final int DEFAULT_DELAY = 60;
        NestedPullRefreshLayout layout;

        long currentDelayTime;

        public OverScrollHandler(NestedPullRefreshLayout layout) {
            this.layout = layout;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            float velocityY = (float) msg.obj;
            switch (msg.what) {
                case MSG_START_COMPUTE_SCROLL:
                    currentDelayTime = -1;
                case MSG_CONTINUE_COMPUTE_SCROLL: {
                    currentDelayTime++;
                    if (!layout.canChildScrollUp()) {
                        layout.overScroll(velocityY, currentDelayTime, true/*scroll To Top*/);
                        currentDelayTime = DEFAULT_DELAY;
                    }
                    if (!layout.canChildScrollDown()) {
                        layout.overScroll(velocityY, currentDelayTime, false/*scroll To Bottom*/);
                        currentDelayTime = DEFAULT_DELAY;
                    }
                    if (currentDelayTime < DEFAULT_DELAY) {
                        sendMessageDelayed(obtainMessage(MSG_CONTINUE_COMPUTE_SCROLL, velocityY), 10);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }
}

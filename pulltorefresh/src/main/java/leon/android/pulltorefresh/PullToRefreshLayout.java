package leon.android.pulltorefresh;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Scroller;

import static leon.android.pulltorefresh.State.STATE_COMPLETE;
import static leon.android.pulltorefresh.State.STATE_IDLE;
import static leon.android.pulltorefresh.State.STATE_PULL_TO_REFRESH;
import static leon.android.pulltorefresh.State.STATE_REFRESHING;
import static leon.android.pulltorefresh.State.STATE_RELEASE_TO_REFRESH;

/**
 * Created by Administrator on 2017/6/23.
 */

public class PullToRefreshLayout extends FrameLayout implements IPullToRefresh {

    private static final float DEFAULT_OFFSET_RATIO = 2.0f;
    private static final int DEFAULT_SCROLL_DURATION = 1000;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mMaxVelocity;
    private IRefreshHeader mRefreshHeader;
    @State
    private int mState;

    /**
     * 滑动刷新控件
     */
    private View mTarget;
    /**
     * 触控点
     */
    private float mLastMotionY;
    /**
     * 偏移量
     */
    private int mOffset;
    /**
     * Scroll偏移量
     */
    private int mLastScrollY;

    private OnChildScrollCallback<PullToRefreshLayout> mCallback;
    private OnRefreshListener mOnRefreshListener;

    public PullToRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
//        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
        mVelocityTracker = VelocityTracker.obtain();
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mState = STATE_IDLE;
    }

    @Override
    public void setRefreshHeader(@NonNull IRefreshHeader refreshHeader) {
        if (mRefreshHeader != null)
            removeView(mRefreshHeader.getView());
        if (refreshHeader.getView() == null)
            throw new NullPointerException("header view should not be Null");
        mRefreshHeader = refreshHeader;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mRefreshHeader.getView(), 0, params);
        ensureTarget();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mRefreshHeader == null) return super.dispatchTouchEvent(ev);
        mVelocityTracker.addMovement(ev);
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = ev.getY();
                if (mState != STATE_REFRESHING) {
                    setState(STATE_IDLE);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float delta = ev.getY() - mLastMotionY;
                mLastMotionY = ev.getY();
                boolean moveDown = delta > 0;
                if (moveDown && canChildScrollUp()) {
                    return super.dispatchTouchEvent(ev);
                }
                if (!moveDown && mOffset <= 0)
                    return super.dispatchTouchEvent(ev);

                offsetViewTopAndBottom((int) (delta / DEFAULT_OFFSET_RATIO));
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mOffset <= 0) {
                    return super.dispatchTouchEvent(ev);
                }
                mLastScrollY = 0;
                if (mState == STATE_REFRESHING) {
                    if (mOffset > mRefreshHeader.getRefreshThreshold())
                        mScroller.startScroll(0, 0, 0, mOffset - mRefreshHeader.getRefreshThreshold(), DEFAULT_SCROLL_DURATION);
                    else
                        mScroller.startScroll(0, 0, 0, mOffset, DEFAULT_SCROLL_DURATION);
                } else if (mState == STATE_RELEASE_TO_REFRESH) {
                    setState(STATE_REFRESHING);
                    mScroller.startScroll(0, 0, 0, mOffset - mRefreshHeader.getRefreshThreshold(), DEFAULT_SCROLL_DURATION);
                } else {
                    mScroller.startScroll(0, 0, 0, mOffset, DEFAULT_SCROLL_DURATION);
                }
                postInvalidate();
                return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void offsetViewTopAndBottom(int offset) {
        mOffset += offset;
        if (mOffset <= 0) {
            mOffset = 0;
            return;
        }
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            ViewCompat.offsetTopAndBottom(child, offset);
        }
        requestLayout();
        if (mState != STATE_REFRESHING && mState != STATE_COMPLETE) {
            if (mOffset < mRefreshHeader.getRefreshThreshold()) {
                setState(STATE_PULL_TO_REFRESH);
            } else {
                setState(STATE_RELEASE_TO_REFRESH);
            }
        }
        mRefreshHeader.onPositionChanged(mOffset / (float) mRefreshHeader.getRefreshThreshold(), mState);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && mTarget instanceof AbsListView)
                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int offset = mScroller.getCurrY() - mLastScrollY;
            mLastScrollY = mScroller.getCurrY();
            offsetViewTopAndBottom(-offset);
            postInvalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        View headerView = null;
        if (mRefreshHeader != null) {
            headerView = mRefreshHeader.getView();
            headerView.layout(left, mOffset - headerView.getMeasuredHeight(), right, mOffset);
        }
        ensureTarget();
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child != headerView) {
                child.layout(left, mOffset, right, bottom + mOffset - top);
            }
        }
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
    }

    public boolean canChildScrollUp() {
        if (mCallback != null) {
            return mCallback.canChildScrollUp(this, mTarget);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.canScrollVertically(-1) || mTarget.getScrollY() > 0;
            }
        } else {
            return mTarget.canScrollVertically(-1);
        }
    }

    protected void setState(@State int state) {
        if (mState != state) {
            mState = state;
            onStateChanged(state);
        }
    }

    private void onStateChanged(@State int state) {
        if (mOnRefreshListener != null && mState == STATE_REFRESHING)
            mOnRefreshListener.onRefresh();
        if (mRefreshHeader == null || mRefreshHeader.getStateChangedListener() == null)
            return;
        final OnStateChangedListener listener = mRefreshHeader.getStateChangedListener();
        listener.onStateChanged(state);
    }

    @Override
    public void refreshComplete() {
        setState(STATE_COMPLETE);
        mLastScrollY = 0;
        mScroller.startScroll(0, 0, 0, mOffset, DEFAULT_SCROLL_DURATION);
        postInvalidate();
    }

    @Override
    public void setOnChildScrollCallback(OnChildScrollCallback callback) {
        mCallback = callback;
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }
}

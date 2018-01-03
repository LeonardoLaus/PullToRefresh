package leon.android.pulltorefresh;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import static leon.android.pulltorefresh.State.STATE_COMPLETE;
import static leon.android.pulltorefresh.State.STATE_IDLE;
import static leon.android.pulltorefresh.State.STATE_PULL_TO_REFRESH;
import static leon.android.pulltorefresh.State.STATE_REFRESHING;
import static leon.android.pulltorefresh.State.STATE_RELEASE_TO_REFRESH;

/**
 * @author Administrator
 * @date 2017/6/23
 */

public abstract class AbstractRefreshHeader implements IRefreshHeader, OnStateChangedListener {
    protected Context mContext;
    protected View mHeaderView;

    public AbstractRefreshHeader(@NonNull Context context,
                                 @LayoutRes int layoutId) {
        mContext = context;
        mHeaderView = LayoutInflater.from(context).inflate(layoutId, null);
    }

    @NonNull
    @Override
    public View getView() {
        return mHeaderView;
    }

    @Override
    public OnStateChangedListener getStateChangedListener() {
        return this;
    }

    @Override
    public int getRefreshThreshold() {
        return mHeaderView.getMeasuredHeight();
    }

    @Override
    public void onStateChanged(@State int state) {
        switch (state) {
            case STATE_IDLE:
                onIdle();
                break;
            case STATE_PULL_TO_REFRESH:
                onPullToRefresh();
                break;
            case STATE_RELEASE_TO_REFRESH:
                onReleaseToRefresh();
                break;
            case STATE_REFRESHING:
                onRefreshing();
                break;
            case STATE_COMPLETE:
                onComplete();
                break;
        }
    }

    /**
     * 初始状态
     */
    public abstract void onIdle();

    /**
     * 下拉刷新
     */
    protected abstract void onPullToRefresh();

    /**
     * 释放刷新
     */
    protected abstract void onReleaseToRefresh();

    /**
     * 刷新中
     */
    protected abstract void onRefreshing();

    /**
     * 刷新完成
     */
    protected abstract void onComplete();
}

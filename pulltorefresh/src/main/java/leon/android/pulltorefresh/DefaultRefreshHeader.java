package leon.android.pulltorefresh;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Administrator
 * @date 2017/6/23
 */

public class DefaultRefreshHeader extends AbstractRefreshHeader {

    private static final int ROTATE_ANIM_DURATION = 150;
    private SimpleDateFormat mDateFormat;
    private HeaderViewHolder mViewHolder;
    private Animation mRotateUpAnimation;
    private Animation mRotateDownAnimation;


    public DefaultRefreshHeader(Context context) {
        super(context, R.layout.layout_default_header);
        mContext = context;
        mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        mViewHolder = new HeaderViewHolder();
        mViewHolder.refreshLabel = (TextView) mHeaderView.findViewById(R.id.text_refresh_status);
        mViewHolder.refreshTime = (TextView) mHeaderView.findViewById(R.id.text_refresh_time);
        mViewHolder.refreshImage = (ImageView) mHeaderView.findViewById(R.id.refresh_image);
        mViewHolder.progressBar = (ProgressBar) mHeaderView.findViewById(R.id.refresh_progress_bar);
        mViewHolder.refreshTime.setText(context.getString(R.string.recent_refresh,
                formatDateTime(System.currentTimeMillis())));
        initializeAnimation();
        mViewHolder.progressBar.setVisibility(View.GONE);
    }

    private void initializeAnimation() {
        mRotateUpAnimation = new RotateAnimation(0.0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnimation.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnimation.setFillAfter(true);
        mRotateDownAnimation = new RotateAnimation(-180f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnimation.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnimation.setFillAfter(true);
    }

    private String formatDateTime(long time) {
        if (time == 0) {
            return "";
        }
        return mDateFormat.format(new Date(time));
    }

    @Override
    public void onPositionChanged(float percent, @State int state) {

    }

    @Override
    public void onIdle() {
        mViewHolder.refreshImage.clearAnimation();
        mViewHolder.refreshImage.setVisibility(View.VISIBLE);
        mViewHolder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPullToRefresh() {
        Log.d("Test", " onPullToRefresh=" + mHeaderView.getScrollY());
        mViewHolder.refreshLabel.setText(R.string.pull_down);
        if (mViewHolder.refreshImage.getAnimation() != null) {
            mViewHolder.refreshImage.clearAnimation();
            mViewHolder.refreshImage.startAnimation(mRotateDownAnimation);
        }
    }

    @Override
    public void onReleaseToRefresh() {
        mViewHolder.refreshLabel.setText(R.string.release_to_refresh);
        mViewHolder.refreshImage.clearAnimation();
        mViewHolder.refreshImage.startAnimation(mRotateUpAnimation);
    }

    @Override
    public void onRefreshing() {
        mViewHolder.refreshImage.clearAnimation();
        mViewHolder.refreshImage.setVisibility(View.GONE);
        mViewHolder.progressBar.setVisibility(View.VISIBLE);
        mViewHolder.refreshLabel.setText(R.string.refreshing);
    }

    @Override
    public void onComplete() {
        mViewHolder.refreshImage.clearAnimation();
        mViewHolder.refreshImage.setVisibility(View.GONE);
        mViewHolder.progressBar.setVisibility(View.VISIBLE);
        mViewHolder.refreshLabel.setText(R.string.refresh_complete);
        mViewHolder.refreshTime.setText(mContext.getString(R.string.recent_refresh,
                formatDateTime(System.currentTimeMillis())));
    }

    private static class HeaderViewHolder {
        TextView refreshLabel;
        TextView refreshTime;
        ImageView refreshImage;
        ProgressBar progressBar;
    }
}

package leon.android.pulltorefresh;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by Administrator on 2017/6/23.
 */

public interface IRefreshHeader {
    @NonNull
    View getView();

    int getRefreshThreshold();

    OnStateChangedListener getStateChangedListener();

    void onPositionChanged(float percent, @State int state);
}

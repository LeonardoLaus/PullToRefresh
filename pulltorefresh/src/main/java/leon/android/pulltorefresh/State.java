package leon.android.pulltorefresh;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static leon.android.pulltorefresh.State.STATE_COMPLETE;
import static leon.android.pulltorefresh.State.STATE_IDLE;
import static leon.android.pulltorefresh.State.STATE_PULL_TO_REFRESH;
import static leon.android.pulltorefresh.State.STATE_REFRESHING;
import static leon.android.pulltorefresh.State.STATE_RELEASE_TO_REFRESH;


/**
 * Created by roothost on 2018/1/3.
 */
@IntDef({STATE_IDLE,
        STATE_PULL_TO_REFRESH,
        STATE_RELEASE_TO_REFRESH,
        STATE_REFRESHING,
        STATE_COMPLETE})
@Retention(RetentionPolicy.SOURCE)
public @interface State {
    int STATE_IDLE = 0;
    int STATE_PULL_TO_REFRESH = 1;
    int STATE_RELEASE_TO_REFRESH = 2;
    int STATE_REFRESHING = 3;
    int STATE_COMPLETE = 4;
}

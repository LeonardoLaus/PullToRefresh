package leon.android.pulltorefresh;

import android.support.annotation.NonNull;

/**
 * Created by Administrator on 2017/6/27.
 */

interface IPullToRefresh {

    void setRefreshHeader(@NonNull IRefreshHeader refreshHeader);

    void refreshComplete();

    void setOnChildScrollCallback(OnChildScrollCallback callback);

    void setOnRefreshListener(OnRefreshListener listener);
}

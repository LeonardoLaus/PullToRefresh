package leon.android.pulltorefresh;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/6/27.
 */

public interface OnChildScrollCallback<Parent extends ViewGroup> {
    /**
     * Callback that will be called when  Parent#canChildScrollUp() method
     * is called to allow the implementer to override its behavior.
     *
     * @param parent parent that this callback is overriding.
     * @param child  The child view of parent.
     * @return Whether it is possible for the child view of parent layout to scroll up.
     */
    boolean canChildScrollUp(@NonNull Parent parent, @Nullable View child);

    /**
     * Callback that will be called when  Parent#canChildScrollDown() method
     * is called to allow the implementer to override its behavior.
     *
     * @param parent parent that this callback is overriding.
     * @param child  The child view of parent.
     * @return Whether it is possible for the child view of parent layout to scroll up.
     */
    boolean canChildScrollDown(@NonNull Parent parent, @Nullable View child);
}

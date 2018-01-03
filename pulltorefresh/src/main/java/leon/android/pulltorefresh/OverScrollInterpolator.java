package leon.android.pulltorefresh;

import android.view.animation.Interpolator;

/**
 * Created by Administrator on 2017/6/27.
 */

public class OverScrollInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        return (float) (1.2f * Math.sin(input * Math.PI));
    }
}

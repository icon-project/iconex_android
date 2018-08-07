package foundation.icon.iconex.widgets.RefreshLayout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 6. 14..
 */

public class LoadingHeaderView extends FrameLayout implements LoadingHeaderListener {

    private static final String TAG = LoadingHeaderView.class.getSimpleName();

    private final ImageView mRefreshCircle;
    private final ImageView mRefreshOrbit;
    private final ProgressBar mRefresh;

    public LoadingHeaderView(@NonNull Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.layout_loading_header, this, true);
        mRefreshCircle = findViewById(R.id.img_refresh_circle);
        mRefreshOrbit = findViewById(R.id.img_refresh_orbit);
        mRefresh = findViewById(R.id.prog_refresh);
    }

    @Override
    public void onRefreshBefore(int scrollY, int headerHeight) {
        mRefreshCircle.setVisibility(VISIBLE);
        mRefreshOrbit.setVisibility(VISIBLE);
        mRefresh.setVisibility(INVISIBLE);
        if (Math.abs(scrollY) < headerHeight * 0.4f) {
            return;
        }

        float y = Math.abs(scrollY + headerHeight * 0.4f);
        float degree = Math.abs(y * 360 / (headerHeight * 0.6f));

        float opacity = Math.abs((headerHeight * 0.6f - y) / (headerHeight * 0.6f));

        mRefreshCircle.setRotation(degree);
        mRefreshOrbit.setRotation(-degree);
        mRefreshOrbit.setAlpha(opacity);
    }

    @Override
    public void onRefreshAfter(int scrollY, int headerHeight) {
//        mHeaderImageView.setBackgroundDrawable(mFrameAnimation.getFrame(mNumberOfFrames - 1));
    }

    @Override
    public void onRefreshReady(int scrollY, int headerHeight) {

    }

    @Override
    public void onRefreshing(int scrollY, int headerHeight) {
        mRefreshCircle.setVisibility(INVISIBLE);
        mRefreshOrbit.setVisibility(INVISIBLE);
        mRefresh.setVisibility(VISIBLE);
    }

    @Override
    public void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess) {
//        Drawable background = mHeaderImageView.getBackground();
//        if (background instanceof AnimationDrawable) {
//            ((AnimationDrawable) background).stop();
//        }
    }

    @Override
    public void onRefreshCancel(int scrollY, int headerHeight) {

    }

    @Override
    public int getRefreshHeight() {
        return 0;
    }
}

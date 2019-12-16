package foundation.icon.iconex.view.ui.mainWallet.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.widgets.RefreshLayout.LoadingHeaderListener;

public class RefreshLoadingView extends FrameLayout implements LoadingHeaderListener {
    public RefreshLoadingView(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_pull_to_refreash, this, true);
    }

    @Override
    public void onRefreshBefore(int scrollY, int headerHeight) {

    }

    @Override
    public void onRefreshAfter(int scrollY, int headerHeight) {

    }

    @Override
    public void onRefreshReady(int scrollY, int headerHeight) {

    }

    @Override
    public void onRefreshing(int scrollY, int headerHeight) {

    }

    @Override
    public void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess) {

    }

    @Override
    public void onRefreshCancel(int scrollY, int headerHeight) {

    }

    @Override
    public int getRefreshHeight() {
        return 0;
    }
}

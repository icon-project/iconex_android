package foundation.icon.iconex.widgets.RefreshLayout;

/**
 * Created by js on 2018. 6. 20..
 */

public interface LoadingHeaderListener {

    void onRefreshBefore(int scrollY, int headerHeight);


    void onRefreshAfter(int scrollY, int headerHeight);


    void onRefreshReady(int scrollY, int headerHeight);


    void onRefreshing(int scrollY, int headerHeight);

    void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess);


    void onRefreshCancel(int scrollY, int headerHeight);

    int getRefreshHeight();
}

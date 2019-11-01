package foundation.icon.iconex.view.ui.mainWallet;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.dialogs.WalletPasswordDialog;
import foundation.icon.iconex.util.ScreenUnit;
import foundation.icon.iconex.view.AboutActivity;
import foundation.icon.iconex.view.WalletDetailActivity;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletPageAdapter;
import foundation.icon.iconex.view.ui.mainWallet.component.ExpandableViewPager;
import foundation.icon.iconex.view.ui.mainWallet.component.FloatingRRepsMenu;
import foundation.icon.iconex.view.ui.mainWallet.component.RefreshLoadingView;
import foundation.icon.iconex.view.ui.mainWallet.component.SideMenu;
import foundation.icon.iconex.view.ui.mainWallet.component.TotalAssetInfoView;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletAddressCardView;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletCardView;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletIndicator;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletManageMenuDialog;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.EntryViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletViewData;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener;
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout;
import loopchain.icon.wallet.core.Constants;

import static foundation.icon.iconex.barcode.BarcodeCaptureActivity.PARAM_SCANTYPE;

public class MainWalletFragment extends Fragment {
    public static final String TAG = MainWalletFragment.class.getSimpleName();

    public static final int REQ_DETAIL = 10405;

    public enum ViewMode {
        walletView, tokenView
    }
    private ViewMode viewMode = ViewMode.walletView;

    public enum ExchangeUnit {
        USD, BTC, ETH
    }
    private ExchangeUnit exchangeUnit = ExchangeUnit.USD;

    SparseArray<WalletCardView> walletViews = new SparseArray<>();

    public interface RequestActivity {
        void refreshViewData();
        void patchViewData();
        void changeExchangeUnit(String unit);
        void fragmentResume();
        void fragmentStop();
    }

    // UI field
    private DrawerLayout drawer;
    private CustomActionBar actionBar;
    private RefreshLayout refresh;
    private TotalAssetInfoView totalAssetInfoView;
    private ExpandableViewPager walletViewPager;
    private WalletIndicator walletIndicator;
    private WalletAddressCardView walletAddressCard;

    private SideMenu sideMenu;
    private FloatingRRepsMenu prepsMenu;

    private List<WalletViewData> walletVDs;
    private List<WalletViewData> tokenListVDs;

    private WalletPageAdapter adapter = null;

    public void updateAssetsVD(TotalAssetsViewData totalAssetsVD) {
        totalAssetInfoView.bind(totalAssetsVD);
    }

    public void initWalletVDs(List<WalletViewData> walletVDs, List<WalletViewData> tokenListVDs) {
        this.walletVDs = walletVDs;
        this.tokenListVDs = tokenListVDs;
        updateWalletView();
    }

    public void updateWallet(List<Integer> wallets, List<Integer> tokens) {
        List<Integer> update = new ArrayList<>();
        update.addAll(viewMode == ViewMode.walletView ? wallets : tokens);
        for (int pos : update) adapter.notifyItemChanged(pos);
    }

    public void updateAllWallet() {
        adapter.notifyDataSetChanged();
    }

    public void notifyCompleteDataLoad() {
        updateAllWallet();
        refresh.stopRefresh(true);
    }

    public static MainWalletFragment newInstance() {
        return new MainWalletFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.main_wallet_frgment, container, false);

        // load UI
        drawer = v.findViewById(R.id.drawer);
        actionBar = v.findViewById(R.id.actionbar);
        refresh = v.findViewById(R.id.refresh);
        totalAssetInfoView = v.findViewById(R.id.info_total_asset);
        walletViewPager = v.findViewById(R.id.wallet_viewpager);
        walletIndicator = v.findViewById(R.id.wallet_indicator);
        walletAddressCard = v.findViewById(R.id.wallet_address_card);

        sideMenu = v.findViewById(R.id.side_menu);
        prepsMenu = v.findViewById(R.id.floating_menu);

        initView(v);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((RequestActivity) getActivity()).fragmentResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((RequestActivity) getActivity()).fragmentStop();
    }

    private void initView (View content) {
        sideMenu.bindDrawer(drawer);
        actionBar.setOnActionClickListener(new CustomActionBar.OnActionClickListener() {
            @Override
            public void onClickAction(CustomActionBar.ClickAction action) {
                switch (action) {
                    case btnStart: {
                        drawer.openDrawer(Gravity.LEFT);
                    } break;
                    case btnEnd: {
                        showInfo();
                    } break;
                    case btnToggle: {
                        toggleViewMode();
                    } break;
                }
            }
        });

        refresh.addHeader(new RefreshLoadingView(getContext()) {
            @Override // for fix ExpanableViewpager bug
            public void onRefreshBefore(int scrollY, int headerHeight) {
              super.onRefreshBefore(scrollY, headerHeight);
              walletViewPager.setIsExpanable(false);
            }

            @Override
            public void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess) {
              super.onRefreshComplete(scrollY, headerHeight, isRefreshSuccess);
              walletViewPager.setIsExpanable(true);
            }

            @Override
            public void onRefreshCancel(int scrollY, int headerHeight) {
              super.onRefreshCancel(scrollY, headerHeight);
              walletViewPager.setIsExpanable(true);
            }
        });

        refresh.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshViewData();
                }

                @Override
                public void onLoadMore() {
                }
            });
        refresh.setRefreshEnable(true);

        totalAssetInfoView.setOnClickExchangeUnitButton(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeExchnageUnit();
            }
        });

        walletAddressCard.setOnDismissListener(new WalletAddressCardView.OnDismissListener() {
            @Override
            public void onDismissStart() {
                Animator aniShow = AnimatorInflater.loadAnimator(getContext(), R.animator.wallet_card_flip_show);
                aniShow.setTarget(walletViewPager);
                walletViewPager.setVisibility(View.VISIBLE);
                aniShow.start();
            }

            @Override
            public void onDismissFinish() {
                updateShowPRepsMenu();
            }
        });

        initWalletViewPager(content);
    }


    private void initWalletViewPager(View content) {
        int dp10 = ScreenUnit.dp2px(getContext(), 10);
        walletViewPager.setClipToPadding(false);
        walletViewPager.setPadding(dp10, 0, dp10, 0);
        walletViewPager.setPageMargin(dp10);
        walletViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected() called with: position = [" + position + "]");
                walletIndicator.setIndex(position);
                updateCollapsable();
                updateShowPRepsMenu();
            }
        });
        walletViewPager.setOnStateChangeListener(new ExpandableViewPager.OnStateChangeListener() {
            @Override
            public void onChangeState(ExpandableViewPager.State state) {
                Log.d("onChangeState", state.name());
                switch (state) {
                    case Expaned: {
                        refresh.setRefreshEnable(false);
                        actionBar.setIsShowIcToggle(false);
                    }
                    break;
                    case Collapsed: {
                        refresh.setRefreshEnable(true);
                        actionBar.setIsShowIcToggle(true);
                    }
                    break;
                }
            }
        });
        adapter = new WalletPageAdapter(walletViewPager, new WalletPageAdapter.WalletViewEventListener() {
            @Override
            public void onChangeIsScrollTop(boolean isScrollTop) {
                updateCollapsable();
            }

            @Override
            public void onClickWalletItem(EntryViewData entryVD) {
                if (entryVD.getWallet() == null || entryVD.getEntry() == null) return;

                getActivity().startActivityForResult(
                        new Intent(getContext(), WalletDetailActivity.class)
                                .putExtra(WalletDetailActivity.PARAM_WALLET, ((Serializable) entryVD.getWallet()))
                                .putExtra(WalletDetailActivity.PARAM_WALLET_ENTRY, ((Serializable) entryVD.getEntry())),
                        REQ_DETAIL
                );
            }

            @Override
            public void onClickQrScan(WalletViewData walletVD) {
                Wallet wallet = walletVD.getWallet();
                new WalletPasswordDialog(getContext(), wallet, new WalletPasswordDialog.OnPassListener() {
                    @Override
                    public void onPass(byte[] bytePrivateKey) {
                        getContext() // only icx wallet, icx coin wallet entry.
                                .startActivity(new Intent(getContext(), BarcodeCaptureActivity.class)
                                        .putExtra(PARAM_SCANTYPE, BarcodeCaptureActivity.ScanType.ICX_Address.name())
                                        .putExtra("wallet", (Serializable) wallet)
                                        .putExtra("entry", (Serializable) wallet.getWalletEntries().get(0))
                                        .putExtra("privateKey", Hex.toHexString(bytePrivateKey))
                                );
                    }
                }).show();
            }

            @Override
            public void onClickQrCode(WalletViewData walletVD) {
                Wallet wallet = walletVD.getWallet();
                prepsMenu.setEnableFloatingButton(false);
                walletAddressCard.show(wallet, wallet.getWalletEntries().get(0));

                Animator aniDisappear = AnimatorInflater.loadAnimator(getContext(), R.animator.wallet_card_flip_disappear);
                aniDisappear.setTarget(walletViewPager);
                aniDisappear.start();
                aniDisappear.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        walletViewPager.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onClickMore(WalletViewData walletVD) {
                Wallet wallet = walletVD.getWallet();
                new WalletManageMenuDialog(getActivity(), wallet, new WalletManageMenuDialog.OnNotifyWalletDataChangeListener() {
                    @Override
                    public void onNotifyWalletDataChange(WalletManageMenuDialog.UpdateDataType updateDataType) {
                        switch (updateDataType) {
                            case Delete: {
                                walletViewPager.setCurrentItem(0);
                                ((RequestActivity) getActivity()).refreshViewData();
                            }
                            case Rename: {
                                ((RequestActivity) getActivity()).patchViewData();
                            }
                        }
                    }
                }).show();
            }
        });
        walletViewPager.setAdapter(adapter);

        float scale = getContext().getResources().getDisplayMetrics().density;
        walletViewPager.setCameraDistance(scale * 8000);

        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                content.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Rect pagerBound = new Rect();
                Rect infoBound = new Rect();
                walletViewPager.getGlobalVisibleRect(pagerBound);
                totalAssetInfoView.getGlobalVisibleRect(infoBound);
                walletViewPager.setExpandedHeight(pagerBound.bottom - infoBound.top);
                walletViewPager.setCollapseHeight(pagerBound.bottom - infoBound.bottom);
            }
        });
    }

    private void refreshViewData() {
        ((RequestActivity) getActivity()).refreshViewData();
    }

    private void changeExchnageUnit() {
        switch (exchangeUnit) {
            case USD:
                exchangeUnit = ExchangeUnit.BTC;
                break;
            case BTC:
                exchangeUnit = ExchangeUnit.ETH;
                break;
            case ETH:
                exchangeUnit = ExchangeUnit.USD;
                break;
        }

        ((RequestActivity) getActivity()).changeExchangeUnit(exchangeUnit.name());
    }

    private void updateCollapsable() {
        int position = walletViewPager.getCurrentItem();
        WalletCardView walletCardView = walletViews.get(position);
        boolean collapsable = walletCardView == null || walletCardView.getIsScrollTop();
        walletViewPager.setIsCollapsable(collapsable);
    }

    private void updateShowPRepsMenu() {
        WalletViewData walletVD = adapter.getCurrentViewData();

        boolean isICX = !walletAddressCard.isShow() &&
                walletVD.getWallet() != null &&
                walletVD.getWallet().getCoinType().equals(Constants.KS_COINTYPE_ICX);

        prepsMenu.setEnableFloatingButton(isICX);
        if (isICX) {
            prepsMenu.bind(walletVD.getWallet());
        }
    }

    private void updateWalletView() {
        Log.d(TAG, "updateWalletView() called");
        adapter.walletVDs.clear();
        switch (viewMode) {
            case walletView: {
                adapter.walletVDs.addAll(walletVDs);
                actionBar.setTitle(getString(R.string.appbarSelectorWallets));
            } break;
            case tokenView: {
                adapter.walletVDs.addAll(tokenListVDs);
                actionBar.setTitle(getString(R.string.appbarSelectorCoinsNTokens));
            } break;
        }

        adapter.notifyDataSetChanged();

        updateShowPRepsMenu();
        walletIndicator.setSize(adapter.walletVDs.size());
    }

    private void toggleViewMode() {
        switch (viewMode) {
            case walletView: viewMode = ViewMode.tokenView; break;
            case tokenView: viewMode = ViewMode.walletView; break;
        }
        walletViewPager.setCurrentItem(0);
        updateWalletView();
    }

    private void showInfo() {
        startActivity(new Intent(getContext(), AboutActivity.class)
                .putExtra(AboutActivity.PARAM_ABOUT_ITEM_LIST, new ArrayList<Parcelable>() {{

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo00)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo01)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo02)));

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo10)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo11)));

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo20)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo21)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo22)));

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo30)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo31)));

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo40)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo41)));
                }}));
    }
}
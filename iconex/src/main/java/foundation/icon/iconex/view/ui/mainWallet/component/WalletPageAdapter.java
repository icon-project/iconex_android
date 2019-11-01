package foundation.icon.iconex.view.ui.mainWallet.component;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.view.ui.mainWallet.viewdata.EntryViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletViewData;

public class WalletPageAdapter extends PagerAdapter {

    final public List<WalletViewData> walletVDs = new ArrayList<>();
    final private List<WalletCardView> mWalletViews = new ArrayList<>();
    final private List<WalletCardView> mWalletViewPool = new ArrayList<>();
    final private SparseArray<Boolean> updated = new SparseArray<>();

    final private ViewPager mViewPager;
    final private WalletViewEventListener mListener;

    public WalletPageAdapter(ViewPager viewPager, WalletViewEventListener listener) {
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                internalOnPageSelected();
            }
        });
        mListener = listener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        WalletCardView walletView = mWalletViews.get(position);
        container.addView(walletView);
        return walletView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        WalletCardView walletView = (WalletCardView) object;
        container.removeView(walletView);
    }

    @Override
    public int getCount() {
        return mWalletViews.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        WalletCardView walletView = (WalletCardView) object;
        int pos = mWalletViews.indexOf(walletView);
        return pos >= 0 ? POSITION_UNCHANGED : POSITION_NONE;
    }

    private void internalOnPageSelected() {
        updateVisiblePage();
    }

    @Override
    public void notifyDataSetChanged() {
        updated.clear();
        int needCount = walletVDs.size() - mWalletViews.size();

        if (needCount > 0) {
            for (int i = 0; needCount > i; i++) {
                mWalletViews.add(mWalletViewPool.size() != 0 ?
                        mWalletViewPool.remove(mWalletViewPool.size() -1):
                        newWalletCardView()
                );
            }
            super.notifyDataSetChanged();
        }

        if (needCount < 0) {
            int removeCount = Math.abs(needCount);
            for (int i = 0; removeCount > i; i++) {
                mWalletViewPool.add(mWalletViews.remove(mWalletViews.size() -1));
            }
            super.notifyDataSetChanged();
        }

        updateVisiblePage();
    }

    public void notifyItemChanged(int pos) {
        int cur = mViewPager.getCurrentItem();
        int size = mWalletViews.size();
        int min = cur -1 < 0 ? 0 : cur -1;
        int max = cur +1 >= size ? size -1 : cur +1;
        boolean isUpdateNow = min <= pos && pos <= max;
        if (isUpdateNow) {
            WalletCardView walletView = mWalletViews.get(pos);
            WalletViewData walletVD = walletVDs.get(pos);
            walletView.bindData(walletVD);
        }

        updated.put(cur, isUpdateNow);
    }

    public WalletViewData getCurrentViewData() {
        return walletVDs.get(mViewPager.getCurrentItem());
    }

    public boolean isUpdated(int pos) {
        return updated.get(pos) != null && updated.get(pos);
    }

    private void updateVisiblePage() {
        int pos = mViewPager.getCurrentItem();
        int min = pos -1;
        int max = pos +1;
        for (int i = min; max >= i; i++) {
            if (i < 0 || walletVDs.size() <= i || isUpdated(i)) continue;
            WalletViewData walletVD = walletVDs.get(i);
            WalletCardView walletView = mWalletViews.get(i);
            walletView.bindData(walletVD);
            updated.put(i, true);
        }
    }

    private WalletCardView newWalletCardView() {
        WalletCardView walletView = new WalletCardView(mViewPager.getContext());
        walletView.setOnChagneIsScrollTopListener(new WalletCardView.OnChangeIsScrollTopListener() {
            @Override
            public void onChangeIsScrollTop(boolean isScrollTop) {
                mListener.onChangeIsScrollTop(isScrollTop);
            }
        });

        walletView.setOnClickWalletItemListner(new WalletCardView.OnClickWalletItemListner() {
            @Override
            public void onClickWalletItem(EntryViewData itemViewData) {
                mListener.onClickWalletItem(itemViewData);
            }
        });

        walletView.setOnClickQrScanListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClickQrScan(walletView.getData());
            }
        });

        walletView.setOnClickQrCodeListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClickQrCode(walletView.getData());
            }
        });

        walletView.setOnClickMoreListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClickMore(walletView.getData());
            }
        });

        return walletView;
    }

    public interface WalletViewEventListener{
        void onChangeIsScrollTop(boolean isScrollTop);
        void onClickWalletItem(EntryViewData entryVD);
        void onClickQrScan(WalletViewData walletVD);
        void onClickQrCode(WalletViewData walletVD);
        void onClickMore(WalletViewData walletVD);
    }
}

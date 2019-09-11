package foundation.icon.iconex.dev_mainWallet.items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;

public abstract class WalletItem extends FrameLayout {
    public WalletItem(@NonNull Context context) {
        super(context);
    }

    public WalletItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WalletItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WalletItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract void bind(WalletItemViewData data);
    public abstract void setOnClickWalletItem(View.OnClickListener listener);
}

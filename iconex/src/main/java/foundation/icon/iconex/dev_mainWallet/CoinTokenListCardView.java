package foundation.icon.iconex.dev_mainWallet;

import android.content.Context;

import androidx.annotation.NonNull;

public class CoinTokenListCardView extends WalletCardView {
    public CoinTokenListCardView(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView () {
        btnMore.setVisibility(GONE);
        btnQrCode.setVisibility(GONE);
        btnQrSacn.setVisibility(GONE);
    }
}

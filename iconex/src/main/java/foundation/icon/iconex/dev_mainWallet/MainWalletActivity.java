package foundation.icon.iconex.dev_mainWallet;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;

import foundation.icon.iconex.dev_mainWallet.items.ICXcoinWalletItem;
import foundation.icon.iconex.dev_mainWallet.items.TokenWalletItem;
import foundation.icon.iconex.dev_mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;

public class MainWalletActivity extends AppCompatActivity implements MainWalletFragment.SyncRequester {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = new FrameLayout(this);
        container.setId(R.id.container);
        setContentView(container, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));


        MainWalletFragment fragment = MainWalletFragment.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    @Override // Thread Safe, refresh trigger
    public TotalAssetsViewData onSyncRequestTotalAssetsData() {
        return new TotalAssetsViewData()
                .setTotalAsset(new BigInteger("20000000"))
                .setVotedPower(0.99f);

    }

    @Override // Thread Safe
    public List<WalletCardViewData> onSyncRequestWalletListData() {
        return new ArrayList<WalletCardViewData>() {{
            add(new WalletCardViewData()
                    .setTitle("아이콘 지갑1")
                    .setWalletType(WalletCardViewData.WalletType.ICXwallet)
                    .setLstWallet(new ArrayList<WalletItemViewData>() {{
                        add(new WalletItemViewData()
                                .setWalletItemType(WalletItemViewData.WalletItemType.ICXcoin)
                                .setAmount("1,234.2600")
                                .setExchanged("187.274 USD")
                                .setStacked("70.1")
                                .setVotingPower("1,000.1234")
                                .setiScore("1,234.26000")
                        );
                        add(new WalletItemViewData()
                                .setWalletItemType(WalletItemViewData.WalletItemType.Token)
                                .setSymbol("ABC gogo")
                                .setSymbolLetter('A')
                                .setBgSymbolColor(TokenWalletItem.TokenColor.A.color)
                                .setAmount("1,234.2600")
                                .setExchanged("187.274 USD")
                        );
                        add(new WalletItemViewData()
                                .setWalletItemType(WalletItemViewData.WalletItemType.Token)
                                .setSymbol("Gaglin")
                                .setSymbolLetter('G')
                                .setBgSymbolColor(TokenWalletItem.TokenColor.G.color)
                                .setAmount("1,234.2600")
                                .setExchanged("187.274 USD")
                        );
                    }})
            );
            add(new WalletCardViewData()
                    .setTitle("이더리움 지갑1")
                    .setWalletType(WalletCardViewData.WalletType.ETHwallet)
                    .setLstWallet(new ArrayList<WalletItemViewData>() {{
                        add(new WalletItemViewData()
                                .setWalletItemType(WalletItemViewData.WalletItemType.ETHcoin)
                                .setAmount("1,234.2600")
                                .setExchanged("187.274 USD")
                        );
                        add(new WalletItemViewData()
                                .setWalletItemType(WalletItemViewData.WalletItemType.Token)
                                .setSymbol("ABC gogo")
                                .setSymbolLetter('A')
                                .setBgSymbolColor(TokenWalletItem.TokenColor.A.color)
                                .setAmount("1,234.2600")
                                .setExchanged("187.274 USD")
                        );
                        add(new WalletItemViewData()
                                .setWalletItemType(WalletItemViewData.WalletItemType.Token)
                                .setSymbol("Gaglin")
                                .setSymbolLetter('G')
                                .setBgSymbolColor(TokenWalletItem.TokenColor.G.color)
                                .setAmount("1,234.2600")
                                .setExchanged("187.274 USD")
                        );
                    }})
            );
        }};
    }

    @Override // Thread Safe
    public List<WalletCardViewData> onSyncRequestTokenListData() {
        return new ArrayList<WalletCardViewData>() {{
            add(new WalletCardViewData()
                    .setWalletType(WalletCardViewData.WalletType.TokenList)
                    .setTitle("ICON")
            );
        }};
    }
}
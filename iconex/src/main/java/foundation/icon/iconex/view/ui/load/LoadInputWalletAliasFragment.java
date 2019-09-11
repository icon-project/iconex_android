package foundation.icon.iconex.view.ui.load;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.MainWalletActivity;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.main.MainActivity;
import foundation.icon.iconex.widgets.TTextInputLayout;
import loopchain.icon.wallet.core.Constants;

public class LoadInputWalletAliasFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = LoadInputWalletAliasFragment.class.getSimpleName();

    private TTextInputLayout inputAlias;
    private Button btnComplete, btnBack;

    private final int OK = 0;
    private final int ALIAS_DUP = 1;
    private final int ALIAS_EMPTY = 2;

    private OnInputWalletAliasListener mListener;
    private LoadViewModel vm;
    private JsonObject keystore;

    private String beforeStr;

    public LoadInputWalletAliasFragment() {
        // Required empty public constructor
    }

    public static LoadInputWalletAliasFragment newInstance() {
        return new LoadInputWalletAliasFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(getActivity()).get(LoadViewModel.class);
        keystore = vm.getKeystore().getValue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_input_wallet_name, container, false);
        initView(v);

        return v;
    }

    private void initView(View v) {
        inputAlias = v.findViewById(R.id.input_alias);
        inputAlias.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (s.toString().trim().isEmpty()) {
                        inputAlias.setText("");
                    } else if (s.charAt(0) == ' ') {
                        inputAlias.setText(beforeStr);
                    } else {
                        if (Utils.checkByteLength(s.toString()) > 16) {
                            inputAlias.setText(beforeStr);
                        } else {
                            beforeStr = s.toString();
                        }
                    }
                } else {
                    btnComplete.setEnabled(false);
                }
            }
        });

        inputAlias.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                checkAlias();
            }
        });

        inputAlias.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                checkAlias();
            }
        });

        btnComplete = v.findViewById(R.id.btn_done);
        btnComplete.setOnClickListener(this);
        btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_done:
                saveWallet();
                startActivity(new Intent(getActivity(), MainWalletActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                break;

            case R.id.btn_back:
                mListener.onAliasBack();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInputWalletAliasListener) {
            mListener = (OnInputWalletAliasListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInputWalletNameCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void checkAlias() {
        String alias = Utils.strip(inputAlias.getText());
        if (alias.isEmpty())
            return;

        for (Wallet wallet : ICONexApp.wallets) {
            if (wallet.getAlias().equals(alias)) {
                inputAlias.setError(true, getString(R.string.duplicateWalletAlias));
                return;
            }
        }

        inputAlias.setError(false, null);
        btnComplete.setEnabled(true);

        return;
    }

    private void saveWallet() {
        Wallet wallet = new Wallet();
        wallet.setAlias(inputAlias.getText());
        wallet.setAddress(keystore.get("address").getAsString());
        wallet.setKeyStore(keystore.toString());

        List<WalletEntry> walletEntries = new ArrayList<>();
        WalletEntry coin = new WalletEntry();
        coin.setType(MyConstants.TYPE_COIN);
        coin.setAddress(keystore.get("address").getAsString());

        if (keystore.has("coinType")) {
            wallet.setCoinType(Constants.KS_COINTYPE_ICX);
            coin.setSymbol(Constants.KS_COINTYPE_ICX);
            coin.setName(MyConstants.NAME_ICX);
        } else {
            wallet.setCoinType(Constants.KS_COINTYPE_ETH);
            coin.setSymbol(Constants.KS_COINTYPE_ETH);
            coin.setName(MyConstants.NAME_ETH);
        }
        walletEntries.add(coin);

        wallet.setWalletEntries(walletEntries);
        wallet.setCreatedAt(Long.toString(System.currentTimeMillis()));

        try {
            RealmUtil.addWallet(wallet);
            RealmUtil.loadWallet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnInputWalletAliasListener {
        void onAliasBack();
    }
}

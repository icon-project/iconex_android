package foundation.icon.iconex.wallet.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.BottomSheetMenu;
import foundation.icon.iconex.control.WalletEntry;
import foundation.icon.iconex.control.WalletInfo;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.dialogs.BottomSheetMenuDialog;
import foundation.icon.iconex.dialogs.EditTextDialog;
import foundation.icon.iconex.intro.IntroActivity;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.token.manage.TokenManageActivity;
import foundation.icon.iconex.token.swap.TokenSwapActivity;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.detail.WalletDetailActivity;
import foundation.icon.iconex.wallet.menu.WalletBackUpActivity;
import foundation.icon.iconex.wallet.menu.WalletPwdChangeActivity;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;
import loopchain.icon.wallet.service.crypto.PKIUtils;

import static foundation.icon.iconex.MyConstants.EXCHANGE_USD;

public class WalletFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = WalletFragment.class.getSimpleName();

    private Context mContext;

    private WalletInfo mWalletInfo;
    private List<WalletEntry> mWalletEntries;
    private WalletEntry mToken;

    private TextView txtWalletAlias;
    private TextView txtBalanceUnit;
    private Button btnViewAddress, btnWalletMenu;
    private TextView txtTotalBalance;

    private ViewGroup loadingBalance;

    private RecyclerView entryRecyclerView;
    private WalletRecyclerAdapter entryRecyclerAdapter;

    private final int RC_CHANGE_PWD = 101;
    private final int RC_DETAIL = 201;
    private final int RC_SWAP = 301;

    public WalletFragment() {
        // Required empty public constructor
    }

    public static WalletFragment newInstance(WalletInfo walletInfo) {
        WalletFragment fragment = new WalletFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("mWalletInfo", walletInfo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWalletInfo = (WalletInfo) getArguments().get("mWalletInfo");
            mWalletEntries = mWalletInfo.getWalletEntries();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);

        txtWalletAlias = v.findViewById(R.id.txt_wallet_alias);
        txtWalletAlias.setText(mWalletInfo.getAlias());

        btnViewAddress = v.findViewById(R.id.btn_wallet_address);
        btnViewAddress.setOnClickListener(this);
        btnWalletMenu = v.findViewById(R.id.btn_wallet_menu);
        btnWalletMenu.setOnClickListener(this);

        txtTotalBalance = v.findViewById(R.id.txt_total_balance);
        txtBalanceUnit = v.findViewById(R.id.txt_balance_unit);

        loadingBalance = v.findViewById(R.id.loading_balance);

        entryRecyclerView = v.findViewById(R.id.recycler_coins);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        walletNotifyDataChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wallet_address:
                if (mWalletInfo.getCoinType().equals(Constants.KS_COINTYPE_ETH))
                    ((MainActivity) getActivity()).showWalletAddress(mWalletInfo.getAlias(), MyConstants.PREFIX_ETH + mWalletInfo.getAddress());
                else
                    ((MainActivity) getActivity()).showWalletAddress(mWalletInfo.getAlias(), mWalletInfo.getAddress());

                break;

            case R.id.btn_wallet_menu:
                BottomSheetMenuDialog menuDialog = new BottomSheetMenuDialog(getActivity(), getString(R.string.manageWallet),
                        BottomSheetMenuDialog.SHEET_TYPE.MENU);
                menuDialog.setMenuData(makeMenus());
                menuDialog.setOnItemClickListener(menuListener);
                menuDialog.show();
                break;
        }
    }

    private void setTotalBalance() {
        Double totalBalance = 0.0;
        int cntNoBalance = 0;
        String unit = ((MainActivity) getActivity()).getExchangeUnit();
        for (WalletEntry entry : mWalletInfo.getWalletEntries()) {
            if (!entry.getBalance().isEmpty()) {
                if (entry.getBalance().equals(MyConstants.NO_BALANCE)) {
                    cntNoBalance++;
                } else {
                    try {
                        BigInteger balance = new BigInteger(entry.getBalance());
                        String value = ConvertUtil.getValue(balance, entry.getDefaultDec());
                        Double doubBalance = Double.parseDouble(value);

                        String exchange = entry.getSymbol().toLowerCase() + unit.toLowerCase();
                        String strPrice;
                        if (exchange.equals("etheth"))
                            strPrice = "1";
                        else
                            strPrice = ICONexApp.EXCHANGE_TABLE.get(exchange);

                        if (strPrice != null) {
                            totalBalance += doubBalance * Double.parseDouble(strPrice);
                        }
                    } catch (Exception e) {
                        // Do nothing.
                    }
                }
            }
        }

        if (cntNoBalance == mWalletInfo.getWalletEntries().size()) {
            txtTotalBalance.setText(MyConstants.NO_BALANCE);
        } else {
            if (unit.equals(EXCHANGE_USD)) {
                txtTotalBalance.setText(String.format(Locale.getDefault(), "%,.2f", totalBalance));
            } else {
                txtTotalBalance.setText(String.format(Locale.getDefault(), "%,.4f", totalBalance));
            }
        }

        txtBalanceUnit.setText(unit);
    }

    private ArrayList<BottomSheetMenu> makeMenus() {
        ArrayList<BottomSheetMenu> menus = new ArrayList<>();
        BottomSheetMenu menu = new BottomSheetMenu(R.drawable.ic_edit, mWalletInfo.getAlias());
        menu.setTag(MyConstants.TAG_MENU_ALIAS);
        menus.add(menu);

        menu = new BottomSheetMenu(R.drawable.ic_setting, getString(R.string.menuManageToken));
        menu.setTag(MyConstants.TAG_MENU_TOKEN);
        menus.add(menu);

        menu = new BottomSheetMenu(R.drawable.ic_backup, getString(R.string.menuBackupWallet));
        menu.setTag(MyConstants.TAG_MENU_BACKUP);
        menus.add(menu);

        menu = new BottomSheetMenu(R.drawable.ic_side_lock, getString(R.string.menuChangePwd));
        menu.setTag(MyConstants.TAG_MENU_PWD);
        menus.add(menu);

        menu = new BottomSheetMenu(R.drawable.ic_delete, getString(R.string.menuDeleteWallet));
        menu.setTag(MyConstants.TAG_MENU_REMOVE);
        menus.add(menu);

        return menus;
    }

    private BottomSheetMenuDialog.OnItemClickListener menuListener = new BottomSheetMenuDialog.OnItemClickListener() {
        @Override
        public void onBasicItem(String item) {

        }

        @Override
        public void onCoinItem(int position) {

        }

        @Override
        public void onMenuItem(String tag) {
            switch (tag) {
                case MyConstants.TAG_MENU_ALIAS:
                    editTextDialog = new EditTextDialog(getActivity(), getString(R.string.modWalletAlias));
                    editTextDialog.setHint(getString(R.string.hintWalletAlias));
                    editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.ALIAS);
                    editTextDialog.setAlias(mWalletInfo.getAlias());
                    editTextDialog.setOnConfirmCallback(mAliasDialogCallback);
                    editTextDialog.show();
                    break;

                case MyConstants.TAG_MENU_TOKEN:
                    Intent intent = new Intent(mContext, TokenManageActivity.class);
                    intent.putExtra("walletInfo", (Serializable) mWalletInfo);

                    if (mWalletInfo.getCoinType().equals(Constants.KS_COINTYPE_ICX))
                        intent.putExtra("type", TokenManageActivity.TOKEN_TYPE.IRC);
                    else
                        intent.putExtra("type", TokenManageActivity.TOKEN_TYPE.ERC);

                    startActivity(intent);
                    break;

                case MyConstants.TAG_MENU_BACKUP:
                    editTextDialog = new EditTextDialog(getActivity(), getString(R.string.enterWalletPassword));
                    editTextDialog.setHint(getString(R.string.hintWalletPassword));
                    editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                    editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.BACKUP);
                    editTextDialog.setOnPasswordCallback(mPasswordDialogCallback);
                    editTextDialog.show();
                    break;

                case MyConstants.TAG_MENU_PWD:
                    startActivityForResult(new Intent(getActivity(), WalletPwdChangeActivity.class)
                            .putExtra("walletInfo", (Serializable) mWalletInfo), RC_CHANGE_PWD);
                    break;

                case MyConstants.TAG_MENU_REMOVE:
                    BigInteger asset = getAsset();
                    final Basic2ButtonDialog dialog = new Basic2ButtonDialog(getActivity());
                    if (asset.compareTo(BigInteger.ZERO) == 0) {
                        dialog.setMessage(getString(R.string.removeWallet));
                        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                            @Override
                            public void onOk() {
                                RealmUtil.removeWallet(mWalletInfo.getAddress());
                                try {
                                    RealmUtil.loadWallet();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (ICONexApp.mWallets.size() == 0) {
                                    startActivity(new Intent(getActivity(), IntroActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                } else {
                                    ((MainActivity) getActivity()).notifyWalletChanged();
                                }

                                dialog.dismiss();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        dialog.show();
                    } else {
                        dialog.setMessage(getString(R.string.warningRemoveWallet));
                        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                            @Override
                            public void onOk() {
                                editTextDialog = new EditTextDialog(getActivity(), getString(R.string.enterWalletPassword));
                                editTextDialog.setHint(getString(R.string.hintWalletPassword));
                                editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                                editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.REMOVE);
                                editTextDialog.setOnPasswordCallback(mPasswordDialogCallback);
                                editTextDialog.show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        dialog.show();
                    }
                    break;
            }
        }
    };

    private BigInteger getAsset() {
        BigInteger asset = new BigInteger("0");
        for (WalletEntry entry : mWalletInfo.getWalletEntries()) {
            try {
                BigInteger balance = new BigInteger(entry.getBalance());
                asset = asset.add(balance);
            } catch (Exception e) {
                // Do nothing.
            }
        }

        return asset;
    }

    public void walletNotifyDataChanged() {

        loadingBalance.setVisibility(View.VISIBLE);

        for (WalletInfo wallet : ICONexApp.mWallets) {
            if (wallet.getAddress().equals(mWalletInfo.getAddress())) {
                mWalletInfo = wallet;
                break;
            }
        }

        if (loadingBalance.getVisibility() == View.VISIBLE) {

            boolean isDone = true;
            for (WalletEntry entry : mWalletInfo.getWalletEntries()) {
                if (entry.getBalance().isEmpty()) {
                    isDone = isDone && false;
                } else {
                    isDone = isDone && true;
                }
            }

            if (isDone) {
                loadingBalance.setVisibility(View.GONE);
            }
        }

        setTotalBalance();

        entryRecyclerAdapter = new WalletRecyclerAdapter(getActivity(), mWalletInfo);
        entryRecyclerAdapter.setClickListener(new WalletRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(WalletEntry walletEntry) {
                startActivityForResult(new Intent(getActivity(), WalletDetailActivity.class)
                        .putExtra("walletInfo", (Serializable) mWalletInfo)
                        .putExtra("walletEntry", (Serializable) walletEntry), RC_DETAIL);
            }

            @Override
            public void onRequestSwap(WalletEntry own, WalletEntry coin) {
                mToken = coin;
                BasicDialog dialog = new BasicDialog(getActivity());
                try {
                    BigInteger tBalance = new BigInteger(coin.getBalance());
                    if (tBalance.equals(BigInteger.ZERO)) {
                        dialog.setMessage(getString(R.string.swapMsgHasNoToken));
                        dialog.show();
                        return;
                    }

                    BigInteger eBalance = new BigInteger(own.getBalance());
                    if (eBalance.equals(BigInteger.ZERO)) {
                        dialog.setMessage(getString(R.string.swapMsgNotEnoughFee));
                        dialog.show();
                        return;
                    }

                    editTextDialog = new EditTextDialog(getActivity(), getString(R.string.enterWalletPassword));
                    editTextDialog.setHint(getString(R.string.hintWalletPassword));
                    editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                    editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.SWAP);
                    editTextDialog.setOnPasswordCallback(mPasswordDialogCallback);
                    editTextDialog.show();
                } catch (Exception e) {
                    dialog.setMessage(getString(R.string.swapMsgHasNoToken));
                    dialog.show();
                }
            }
        });
        entryRecyclerView.setAdapter(entryRecyclerAdapter);
    }

    public String getAddress() {
        return mWalletInfo.getAddress();
    }

    private EditTextDialog editTextDialog;

    private EditTextDialog.OnConfirmCallback mAliasDialogCallback = new EditTextDialog.OnConfirmCallback() {
        @Override
        public void onConfirm(String alias) {
            if (alias.isEmpty()) {
                editTextDialog.setError(getString(R.string.errWhiteSpace));
                return;
            }

            if (alias.trim().length() == 0) {
                editTextDialog.setError(getString(R.string.errWhiteSpace));
                return;
            }

            for (WalletInfo info : ICONexApp.mWallets) {
                if (info.getAlias().equals(alias)) {
                    editTextDialog.setError(getString(R.string.duplicateWalletAlias));
                    return;
                }
            }

            RealmUtil.modWalletAlias(mWalletInfo.getAddress(), alias);
            mWalletInfo.setAlias(alias);
            txtWalletAlias.setText(mWalletInfo.getAlias());
            ((MainActivity) getActivity()).refreshNameView();
            editTextDialog.dismiss();
        }
    };

    private EditTextDialog.OnPasswordCallback mPasswordDialogCallback = new EditTextDialog.OnPasswordCallback() {
        @Override
        public void onConfirm(EditTextDialog.RESULT_PWD result, String pwd) {
            JsonObject keyStore = new Gson().fromJson(mWalletInfo.getKeyStore(), JsonObject.class);
            byte[] bytePrivKey;
            try {
                JsonObject crypto = null;
                if (keyStore.has("crypto"))
                    crypto = keyStore.get("crypto").getAsJsonObject();
                else
                    crypto = keyStore.get("Crypto").getAsJsonObject();

                bytePrivKey = KeyStoreUtils.decryptPrivateKey(pwd, mWalletInfo.getAddress(), crypto, mWalletInfo.getCoinType());
                if (bytePrivKey != null) {
                    if (result == EditTextDialog.RESULT_PWD.BACKUP) {
                        startActivity(new Intent(getActivity(), WalletBackUpActivity.class)
                                .putExtra("walletInfo", (Serializable) mWalletInfo)
                                .putExtra("privateKey", Hex.toHexString(bytePrivKey)));
                    } else if (result == EditTextDialog.RESULT_PWD.SWAP) {

                        try {
                            Intent swapIntent = new Intent(getActivity(), TokenSwapActivity.class);
                            swapIntent.putExtra(TokenSwapActivity.ARG_WALLET, (Serializable) mWalletInfo);
                            swapIntent.putExtra(TokenSwapActivity.ARG_TOKEN, (Serializable) mToken);
                            String ICXAddr = PKIUtils.makeAddressFromPrivateKey(bytePrivKey, Constants.KS_COINTYPE_ICX);
                            if (hasSwapWallet(ICXAddr))
                                swapIntent.putExtra(TokenSwapActivity.ARG_TYPE, TokenSwapActivity.TYPE_SWAP.EXIST);
                            else
                                swapIntent.putExtra(TokenSwapActivity.ARG_TYPE, TokenSwapActivity.TYPE_SWAP.NO_WALLET);
                            swapIntent.putExtra(TokenSwapActivity.ARG_ICX_ADDR, ICXAddr);
                            swapIntent.putExtra(TokenSwapActivity.ARG_PRIV, Hex.toHexString(bytePrivKey));

                            startActivityForResult(swapIntent, RC_SWAP);
                        } catch (Exception e) {
                            // TODO: 2018. 5. 16. Notice error
                        }
                    } else {
                        RealmUtil.removeWallet(mWalletInfo.getAddress());
                        try {
                            RealmUtil.loadWallet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (ICONexApp.mWallets.size() == 0) {
                            startActivity(new Intent(getActivity(), IntroActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        } else {
                            ((MainActivity) getActivity()).notifyWalletChanged();
                        }
                    }

                    editTextDialog.dismiss();
                } else {
                    editTextDialog.setError(getString(R.string.errPassword));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private boolean hasSwapWallet(String address) throws Exception {
        for (WalletInfo wallet : ICONexApp.mWallets) {
            if (address.equals(wallet.getAddress()))
                return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_DETAIL:
                if (resultCode == WalletDetailActivity.RES_REFRESH) {
                    ((MainActivity) getActivity()).notifyWalletChanged();
                }
                break;

            case RC_SWAP:
                if (resultCode == TokenSwapActivity.RES_CREATED)
                    ((MainActivity) getActivity()).notifyWalletChanged();
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

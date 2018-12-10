package foundation.icon.iconex.token.swap;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.SwapConfirmDialog;
import foundation.icon.iconex.dialogs.SwapRequestDoneDialog;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.KeyStoreIO;
import foundation.icon.iconex.wallet.create.CreateWalletStep2Fragment;
import foundation.icon.iconex.wallet.create.CreateWalletStep3Fragment;
import foundation.icon.iconex.wallet.create.CreateWalletStep4Fragment;
import foundation.icon.iconex.menu.ViewWalletInfoActivity;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class TokenSwapActivity extends AppCompatActivity implements SwapGuideFragment.OnSwapStep1Listener, CreateWalletStep2Fragment.OnStep2Listener,
        CreateWalletStep3Fragment.OnStep3Listener, CreateWalletStep4Fragment.OnStep4Listener, SwapRequestFragment.OnSwapRequestListener {

    private static final String TAG = TokenSwapActivity.class.getSimpleName();

    private TextView txtTitle;
    private ViewGroup layoutStep;
    private ViewPager viewPager;
    private SwapStepAdapter stepAdapter;
    private NoWalletSwapStepAdapter noWalletAdapter;

    public static String ARG_WALLET = "ARG_WALLET";
    public static String ARG_TOKEN = "ARG_TOKEN";
    public static String ARG_ICX_ADDR = "ARG_ICX_ADDR";
    public static String ARG_TYPE = "ARG_TYPE";
    public static String ARG_PRIV = "ARG_PRIV";

    private TYPE_SWAP mType;
    private static String mPriv;

    private static Wallet mWallet;
    private static WalletEntry mToken;
    private static String mAddr;

    private boolean isDownloaded = false;

    public static final int RES_CREATED = 1001;

    public enum TYPE_SWAP {
        NO_WALLET,
        EXIST
    }

    private NetworkService mService;
    private boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_swap);

        mWallet = (Wallet) getIntent().getSerializableExtra(ARG_WALLET);
        mToken = (WalletEntry) getIntent().getSerializableExtra(ARG_TOKEN);
        mAddr = getIntent().getStringExtra(ARG_ICX_ADDR);
        mType = (TYPE_SWAP) getIntent().getSerializableExtra(ARG_TYPE);
        mPriv = getIntent().getStringExtra(ARG_PRIV);

        txtTitle = findViewById(R.id.txt_title);
        txtTitle.setText(getString(R.string.titleSwapInstructions));
//        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.titleSwap));
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(TokenSwapActivity.this);
                dialog.setOnDialogListener(mDialogListener);

                if (mType == TYPE_SWAP.EXIST) {
                    dialog.setMessage(getString(R.string.swapMsgCancel));
                } else {
                    if (noWalletAdapter.getItem(viewPager.getCurrentItem()) instanceof SwapRequestFragment) {
                        dialog.setMessage(getString(R.string.swapMsgCancel));
                        setResult(RES_CREATED);
                    } else
                        dialog.setMessage(getString(R.string.swapMsgCreateWalletCancel));
                }
                dialog.show();
            }
        });
        layoutStep = findViewById(R.id.layout_step);

        viewPager = findViewById(R.id.container);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View swapStep = null;

                if (mType == TYPE_SWAP.NO_WALLET) {
                    switch (position) {
                        case 0:
                            swapStep = layoutInflater.inflate(R.layout.layout_no_wallet_swap_step1, null);
                            txtTitle.setText(getString(R.string.titleSwapInstructions));
                            break;

                        case 1:
                            swapStep = layoutInflater.inflate(R.layout.layout_no_wallet_swap_step2, null);
                            txtTitle.setText(getString(R.string.titleCreateSwapWallet));
                            break;

                        case 2:
                            swapStep = layoutInflater.inflate(R.layout.layout_no_wallet_swap_step3, null);
                            txtTitle.setText(getString(R.string.titleCreateSwapWallet));
                            break;

                        case 3:
                            swapStep = layoutInflater.inflate(R.layout.layout_no_wallet_swap_step4, null);
                            txtTitle.setText(getString(R.string.titleCreateSwapWallet));
                            break;

                        case 4:
                            swapStep = layoutInflater.inflate(R.layout.layout_no_wallet_swap_step5, null);
                            txtTitle.setText(getString(R.string.titleRequestSwap));
                            break;
                    }
                } else if (mType == TYPE_SWAP.EXIST) {
                    switch (position) {
                        case 0:
                            swapStep = layoutInflater.inflate(R.layout.layout_swap_step1, null);
                            txtTitle.setText(getString(R.string.titleSwapInstructions));
                            break;

                        case 1:
                            swapStep = layoutInflater.inflate(R.layout.layout_swap_step2, null);
                            txtTitle.setText(getString(R.string.titleRequestSwap));
                            break;
                    }
                }

                layoutStep.removeAllViews();
                layoutStep.addView(swapStep);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View swapStep;
        if (mType == TYPE_SWAP.NO_WALLET) {
            swapStep = layoutInflater.inflate(R.layout.layout_no_wallet_swap_step1, null);
            layoutStep.removeAllViews();
            layoutStep.addView(swapStep);

            noWalletAdapter = new NoWalletSwapStepAdapter(getSupportFragmentManager());
            viewPager.setAdapter(noWalletAdapter);

            wallet = new Wallet();
            wallet.setCoinType(Constants.KS_COINTYPE_ICX);
        } else {
            swapStep = layoutInflater.inflate(R.layout.layout_swap_step1, null);
            layoutStep.removeAllViews();
            layoutStep.addView(swapStep);

            stepAdapter = new SwapStepAdapter(getSupportFragmentManager());
            viewPager.setAdapter(stepAdapter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, NetworkService.class);
        mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private Basic2ButtonDialog.OnDialogListener mDialogListener = new Basic2ButtonDialog.OnDialogListener() {
        @Override
        public void onOk() {
            finish();
        }

        @Override
        public void onCancel() {

        }
    };

    @Override
    public void onStep1a() {
        ((TextView) layoutStep.findViewById(R.id.textView)).setText(R.string.swapStep1a);
    }

    @Override
    public void onStep1b() {
        ((TextView) layoutStep.findViewById(R.id.textView)).setText(R.string.swapStep1b);
    }

    @Override
    public void onStep1Next() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    @Override
    public void onStep2Done(String name, String pwd) {
        CreateKeyStore create = new CreateKeyStore();
        create.execute(name, pwd, mPriv);
    }

    @Override
    public void onStep2Back() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        ((NoWalletSwapStepAdapter) viewPager.getAdapter()).setStep1a();
    }

    @Override
    public void onShowInputMode() {

    }

    @Override
    public void onHideInputMode() {

    }

    @Override
    public void onStep3Next() {
        if (isDownloaded) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        } else {
            Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
            dialog.setMessage(getString(R.string.noBackupKeyStoreFileConfirm));
            dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                @Override
                public void onOk() {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                }

                @Override
                public void onCancel() {

                }
            });
            dialog.show();
        }
    }

    @Override
    public void onStep3Back() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
    }

    @Override
    public void onStep4Back() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
    }

    @Override
    public void onStep4Next() {

        try {
            RealmUtil.addWallet(wallet);
            RealmUtil.loadWallet();

            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStep4Done() {
        // Do nothing.
    }

    @Override
    public void showWalletInfo(String privateKey) {
        startActivity(new Intent(this, ViewWalletInfoActivity.class)
                .putExtra("alias", wallet.getAlias())
                .putExtra("coinName", MyConstants.NAME_ICX)
                .putExtra("address", wallet.getAddress())
                .putExtra("privateKey", privateKey)
                .putExtra("date", wallet.getCreatedAt()));
    }

    @Override
    public void onSwapRequest(final String amount, final String price, final String limit, String fee) {
        Log.d(TAG, "Amount=" + amount + ", price=" + price + ", limit=" + limit + ", fee=" + fee);
        ECKeyPair keyPair = ECKeyPair.create(Hex.decode(mPriv));
        org.web3j.crypto.Credentials credentials = org.web3j.crypto.Credentials.create(keyPair);

        BigInteger value = ConvertUtil.valueToBigInteger(amount, 18);
        SwapConfirmDialog dialog = new SwapConfirmDialog(this, credentials, mToken.getContractAddress(),
                ConvertUtil.getValue(value, 18), fee, new BigInteger(limit), price, MyConstants.ETH_INCINERATION, mAddr);
        dialog.setOnDialogListener(new SwapConfirmDialog.OnDialogListener() {
            @Override
            public void onOk() {
                mService.requestTokenTransfer(Integer.toString(mToken.getId()), price,
                        limit, mToken.getContractAddress(),
                        MyConstants.ETH_INCINERATION, amount,
                        Integer.toString(mToken.getDefaultDec()), mPriv);

                SwapRequestDoneDialog doneDialog = new SwapRequestDoneDialog(TokenSwapActivity.this);
                doneDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        TokenSwapActivity.this.finish();
                    }
                });
                doneDialog.show();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
        dialog.setOnDialogListener(mDialogListener);

        if (mType == TYPE_SWAP.EXIST) {
            dialog.setMessage(getString(R.string.swapMsgCancel));
        } else {
            if (noWalletAdapter.getItem(viewPager.getCurrentItem()) instanceof SwapRequestFragment) {
                dialog.setMessage(getString(R.string.swapMsgCancel));
                setResult(RES_CREATED);
            } else
                dialog.setMessage(getString(R.string.swapMsgCreateWalletCancel));
        }
        dialog.show();
    }

    public boolean backupKeyStoreFile() {
        try {
            JsonObject keyStore = new Gson().fromJson(wallet.getKeyStore(), JsonObject.class);
            KeyStoreIO.exportKeyStore(keyStore, Constants.KS_COINTYPE_ICX);
        } catch (Exception e) {
            e.printStackTrace();
            isDownloaded = false;
            return isDownloaded;
        }

        isDownloaded = true;
        return isDownloaded;
    }

    private Wallet wallet;

    private class CreateKeyStore extends AsyncTask<String, Void, String[]> {
        @Override
        protected void onPostExecute(String[] keyStoreInfo) {
            super.onPostExecute(keyStoreInfo);

            wallet.setAddress(keyStoreInfo[0]);
            String privKey = keyStoreInfo[1];
            wallet.setKeyStore(keyStoreInfo[2]);

            List<WalletEntry> entries = new ArrayList<>();
            WalletEntry coin = new WalletEntry();
            coin.setType(MyConstants.TYPE_COIN);
            coin.setAddress(keyStoreInfo[0]);

            coin.setName(MyConstants.NAME_ICX);
            coin.setSymbol(Constants.KS_COINTYPE_ICX);

            entries.add(coin);

            wallet.setWalletEntries(entries);
            wallet.setCreatedAt(Long.toString(System.currentTimeMillis()));

            noWalletAdapter.setKeyStore(wallet.getKeyStore());
            noWalletAdapter.setAddress(wallet.getAddress());
            noWalletAdapter.setPrivKey(privKey);

            noWalletAdapter.clearEdit();
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);

            // TODO: 2018. 3. 27. Create wallet failed.
        }

        @Override
        protected String[] doInBackground(String... params) {
            String name;
            String pwd;
            String priv;

            name = params[0];
            pwd = params[1];
            priv = params[2];

            wallet.setAlias(name);

            String[] keyStoreInfo;
            keyStoreInfo = KeyStoreUtils.generateICXKeyStoreByPriv(pwd, Hex.decode(priv));

            return keyStoreInfo;
        }
    }

    public static Wallet getWallet() {
        return mWallet;
    }

    public static WalletEntry getToken() {
        return mToken;
    }

    public static String getICXAddr() {
        return mAddr;
    }
}

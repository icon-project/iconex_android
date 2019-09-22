package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.TTextInputLayout;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class WalletPasswordDialog extends MessageDialog {

    private Wallet mWallet;

    private TTextInputLayout mEditPassword;
    private TextView mLnkForgotPassword;

    public interface OnPassListener { void onPass(byte[] bytePrivateKey); }
    private OnPassListener mOnPassListener;

    public WalletPasswordDialog(@NotNull Context context, Wallet wallet, OnPassListener listener) {
        super(context);

        mWallet = wallet;
        mOnPassListener = listener;

        buildDialog();
    }

    private void buildDialog() {
        // set Head
        setHeadText(getContext().getString(R.string.enterWalletPassword));

        // set Content
        View content = View.inflate(getContext(), R.layout.layout_password_dialog_content, null);
        setContent(content);

        // load UI
        mEditPassword = content.findViewById(R.id.edit_password);
        mLnkForgotPassword = content.findViewById(R.id.lnk_forgot_password);

        // set Button
        setSingleButton(false);

        mLnkForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Need to implement move to Forgot Password
                Toast.makeText(getContext(), "TODO: Need to implement move to Forgot Password", Toast.LENGTH_SHORT).show();
            }
        });

        mEditPassword.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() == 0) mEditPassword.setError(false, null);
            }
        });

        setOnConfirmClick(new Function1<View, Boolean>() {
            @Override
            public Boolean invoke(View view) {
                String pwd = mEditPassword.getText();
                JsonObject keyStore = new Gson().fromJson(mWallet.getKeyStore(), JsonObject.class);
                final byte[][] bytePrivKey = new byte[1][1];

                Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        JsonObject crypto = null;
                        if (keyStore.has("crypto"))
                            crypto = keyStore.get("crypto").getAsJsonObject();
                        else
                            crypto = keyStore.get("Crypto").getAsJsonObject();

                        bytePrivKey[0] = KeyStoreUtils.decryptPrivateKey(pwd, mWallet.getAddress(), crypto, mWallet.getCoinType());
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                                if (bytePrivKey[0] != null) {
                                    mOnPassListener.onPass(bytePrivKey[0]);
                                    // return true
                                    dismiss();
                                } else {
                                    mEditPassword.setError(true, getContext().getString(R.string.errPassword));
                                    // return false
                                    // x dismiss();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        });
                return false;
            }
        });
    }
}

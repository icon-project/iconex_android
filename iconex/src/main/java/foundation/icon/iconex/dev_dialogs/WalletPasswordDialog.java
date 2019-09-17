package foundation.icon.iconex.dev_dialogs;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.menu.WalletBackUpActivity;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.TTextInputLayout;
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
                byte[] bytePrivKey;
                try {
                    JsonObject crypto = null;
                    if (keyStore.has("crypto"))
                        crypto = keyStore.get("crypto").getAsJsonObject();
                    else
                        crypto = keyStore.get("Crypto").getAsJsonObject();

                    bytePrivKey = KeyStoreUtils.decryptPrivateKey(pwd, mWallet.getAddress(), crypto, mWallet.getCoinType());
                    if (bytePrivKey != null) {
                        mOnPassListener.onPass(bytePrivKey);
                        return true;
                    } else {
                        mEditPassword.setError(true, getContext().getString(R.string.errPassword));
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }
}

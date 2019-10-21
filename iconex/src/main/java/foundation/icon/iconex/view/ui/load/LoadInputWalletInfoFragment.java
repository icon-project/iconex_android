package foundation.icon.iconex.view.ui.load;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.util.PasswordValidator;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.MainWalletActivity;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.widgets.TTextInputLayout;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;
import loopchain.icon.wallet.service.crypto.PKIUtils;

import static foundation.icon.iconex.util.PasswordValidator.checkPasswordMatch;

public class LoadInputWalletInfoFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = LoadInputWalletInfoFragment.class.getSimpleName();

    private OnInputWalletInfoListener mListener;
    private LoadViewModel vm;

    private TTextInputLayout inputAlias, inputPwd, inputCheck;
    private Button btnComplete;
    private ProgressBar progress;

    private String beforeStr, beforePwd, beforeCheck;

    private final int OK = 0;
    private final int ALIAS_DUP = 1;
    private final int ALIAS_EMPTY = 2;

    public LoadInputWalletInfoFragment() {
        // Required empty public constructor
    }

    public static LoadInputWalletInfoFragment newInstance() {
        return new LoadInputWalletInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(getActivity()).get(LoadViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_load_input_wallet_info, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInputWalletInfoListener) {
            mListener = (OnInputWalletInfoListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInputWalletInfoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initView(View v) {
        inputAlias = v.findViewById(R.id.input_alias);
        inputAlias.setOnFocusChangedListener(new TTextInputLayout.OnFocusReleased() {
            @Override
            public void onReleased() {
                if (checkAlias(inputAlias.getText()) == ALIAS_DUP) {
                    inputAlias.setError(true, getString(R.string.duplicateWalletAlias));
                } else {
                    inputAlias.setError(false, null);
                }
            }
        });
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
        inputAlias.setOnKeyPreImeListener(onKeyPreIme);

        inputPwd = v.findViewById(R.id.input_pwd);
        inputPwd.setOnFocusChangedListener(new TTextInputLayout.OnFocusReleased() {
            @Override
            public void onReleased() {
                int result = PasswordValidator.validatePassword(inputPwd.getText());
                switch (result) {
                    case PasswordValidator.EMPTY:
                        inputPwd.setError(true, getString(R.string.errPwdEmpty));
                        break;

                    case PasswordValidator.LEAST_8:
                        inputPwd.setError(true, getString(R.string.errAtLeast));
                        break;
                    case PasswordValidator.NOT_MATCH_PATTERN:
                        inputPwd.setError(true, getString(R.string.errPasswordPatternMatch));
                        break;

                    case PasswordValidator.HAS_WHITE_SPACE:
                        inputPwd.setError(true, getString(R.string.errWhiteSpace));
                        break;

                    case PasswordValidator.SERIAL_CHAR:
                        inputPwd.setError(true, getString(R.string.errSerialChar));
                        break;
                    default:
                        inputPwd.setError(false, null);
                }
            }
        });
        inputPwd.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (s.charAt(s.length() - 1) == ' ') {
                        inputPwd.setText(s.subSequence(0, s.length() - 1).toString());
                    } else if (s.toString().contains(" ")) {
                        inputPwd.setText(beforePwd);
                    } else {
                        beforePwd = s.toString();
                    }
                } else {
                    btnComplete.setEnabled(false);
                }
            }
        });
        inputPwd.setOnKeyPreImeListener(onKeyPreIme);

        inputCheck = v.findViewById(R.id.input_pwd_check);
        inputCheck.setOnFocusChangedListener(new TTextInputLayout.OnFocusReleased() {
            @Override
            public void onReleased() {
                if (inputCheck.getText().isEmpty()) {
                    btnComplete.setEnabled(false);
                } else {
                    if (inputCheck.getText().isEmpty()) {
                        inputCheck.setError(true, getString(R.string.errPasswordNotMatched));
                    } else {
                        boolean result = PasswordValidator.checkPasswordMatch(inputCheck.getText(),
                                inputCheck.getText());
                        if (!result) {
                            inputCheck.setError(true, getString(R.string.errPasswordNotMatched));
                        } else {
                            inputCheck.setError(false, null);
                        }
                    }
                }
            }
        });
        inputCheck.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (s.charAt(s.length() - 1) == ' ') {
                        inputCheck.setText(s.subSequence(0, s.length() - 1).toString());
                    } else if (s.toString().contains(" ")) {
                        inputCheck.setText(beforeCheck);
                    } else {
                        beforeCheck = s.toString();
                    }
                } else {
                    btnComplete.setEnabled(false);
                }
            }
        });
        inputCheck.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                setCompleteEnable(inputAlias.getText(), inputPwd.getText(), inputCheck.getText());
            }
        });
        inputCheck.setOnKeyPreImeListener(onKeyPreIme);

        btnComplete = v.findViewById(R.id.btn_complete);
        btnComplete.setOnClickListener(this);
        progress = v.findViewById(R.id.progress);

        v.findViewById(R.id.btn_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_complete:
                btnComplete.setEnabled(false);
                progress.setVisibility(View.VISIBLE);

                Observable.just(0).map(new Function<Integer, Wallet>() {
                    @Override
                    public Wallet apply(Integer integer) throws Exception {
                        return loadWallet();
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Wallet>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Wallet wallet) {
                                try {
                                    saveWallet(wallet);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    btnComplete.setEnabled(false);
                                    progress.setVisibility(View.VISIBLE);
                                }

                                btnComplete.setEnabled(false);
                                progress.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                btnComplete.setEnabled(false);
                                progress.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onComplete() {
                                startActivity(new Intent(getActivity(), MainWalletActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        });
                break;

            case R.id.btn_back:
                clear();
                mListener.onInfoBack();
                break;
        }
    }

    private TTextInputLayout.OnKeyPreIme onKeyPreIme = new TTextInputLayout.OnKeyPreIme() {
        @Override
        public void onDone() {
            setCompleteEnable(inputAlias.getText(), inputPwd.getText(), inputCheck.getText());
        }
    };

    private int checkAlias(String target) {
        String alias = Utils.strip(target);
        if (alias.isEmpty())
            return ALIAS_EMPTY;

        for (Wallet wallet : ICONexApp.wallets) {
            if (wallet.getAlias().equals(alias))
                return ALIAS_DUP;
        }

        return OK;
    }

    private void setCompleteEnable(String alias, String pwd, String checkPwd) {
        int aliasValidate;
        int pwdValidate;
        boolean matched;

        if (!inputAlias.toString().isEmpty()) {
            aliasValidate = checkAlias(alias);
            switch (aliasValidate) {
                case ALIAS_EMPTY:
                    inputAlias.setError(true, getString(R.string.errAliasEmpty));
                    break;

                case ALIAS_DUP:
                    inputAlias.setError(true, getString(R.string.duplicateWalletAlias));
                    break;
                default:
                    inputAlias.setError(false, null);
            }
        } else {
            inputAlias.setError(true, getString(R.string.errAliasEmpty));
            btnComplete.setEnabled(false);
            return;
        }

        if (!inputPwd.getText().isEmpty()) {
            pwdValidate = PasswordValidator.validatePassword(pwd);
            if (pwdValidate != 0) {
                switch (pwdValidate) {
                    case PasswordValidator.EMPTY:
                        inputPwd.setError(true, getString(R.string.errPwdEmpty));
                        break;

                    case PasswordValidator.LEAST_8:
                        inputPwd.setError(true, getString(R.string.errAtLeast));
                        break;
                    case PasswordValidator.NOT_MATCH_PATTERN:
                        inputPwd.setError(true, getString(R.string.errPasswordPatternMatch));
                        break;

                    case PasswordValidator.HAS_WHITE_SPACE:
                        inputPwd.setError(true, getString(R.string.errWhiteSpace));
                        break;

                    case PasswordValidator.SERIAL_CHAR:
                        inputPwd.setError(true, getString(R.string.errSerialChar));
                        break;
                    default:
                        inputPwd.setError(false, null);
                }
            }
        } else {
            inputPwd.setError(true, getString(R.string.errPwdEmpty));
            btnComplete.setEnabled(false);
            return;
        }

        if (!inputCheck.getText().isEmpty()) {
            matched = checkPasswordMatch(pwd, checkPwd);
            if (!matched) {
                inputCheck.setError(true, getString(R.string.errPasswordNotMatched));
            } else {
                inputCheck.setError(false, null);
            }
        } else {
            inputCheck.setError(true, getString(R.string.errCheckEmpty));
            btnComplete.setEnabled(false);
            return;
        }

        if (aliasValidate == OK && matched && (pwdValidate == PasswordValidator.OK))
            btnComplete.setEnabled(true);
        else
            btnComplete.setEnabled(false);
    }

    private Wallet loadWallet() {
        MyConstants.Coin coin = vm.getCoin().getValue();
        String privateKey = vm.getPrivateKey().getValue();
        Wallet wallet = new Wallet();

        String[] loadedWallet;
        if (coin == MyConstants.Coin.ICX) {
            loadedWallet = KeyStoreUtils.generateICXKeyStoreByPriv(
                    inputPwd.getText(), PKIUtils.hexDecode(privateKey));
        } else {
            loadedWallet = KeyStoreUtils.generateETHKeyStoreByPriv(
                    inputPwd.getText(), PKIUtils.hexDecode(privateKey));
        }

        wallet.setCoinType(coin.getSymbol().toUpperCase());
        wallet.setAlias(inputAlias.getText());
        wallet.setAddress(loadedWallet[0]);
        wallet.setKeyStore(loadedWallet[2]);

        List<WalletEntry> entries = new ArrayList<>();
        WalletEntry entry = new WalletEntry();
        entry.setType(MyConstants.TYPE_COIN);
        entry.setSymbol(coin.getSymbol());
        entry.setAddress(loadedWallet[0]);
        entry.setName(coin.getName());
        entries.add(entry);

        wallet.setWalletEntries(entries);
        wallet.setCreatedAt(Long.toString(System.currentTimeMillis()));

        return wallet;
    }

    private void saveWallet(final Wallet wallet) throws Exception {
        for (Wallet w : ICONexApp.wallets) {
            if (w.getAddress().equals(wallet.getAddress())) {
                RealmUtil.removeWallet(w.getAddress());
            }
        }

        RealmUtil.addWallet(wallet);
        RealmUtil.loadWallet();
    }

    public void clear() {
        inputAlias.setText("");
        inputPwd.setText("");
        inputCheck.setText("");
    }

    public interface OnInputWalletInfoListener {
        void onInfoBack();
    }
}

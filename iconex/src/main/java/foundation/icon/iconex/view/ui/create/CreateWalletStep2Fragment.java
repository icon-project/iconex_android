package foundation.icon.iconex.view.ui.create;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.PasswordValidator;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.widgets.TTextInputLayout;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

import static foundation.icon.iconex.util.PasswordValidator.checkPasswordMatch;

public class CreateWalletStep2Fragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CreateWalletStep2Fragment.class.getSimpleName();

    private OnStep2Listener mListener;
    private CreateWalletViewModel vm;
    private Disposable disposable;

    public static CreateWalletStep2Fragment newInstance() {
        return new CreateWalletStep2Fragment();
    }

    private ScrollView scroll;
    private TTextInputLayout inputAlias, inputPwd, inputCheck;
    private ViewGroup layoutButtons;
    private Button btnBack, btnNext;
    private ProgressBar progress;

    private String beforeStr, beforePwd;

    private final int OK = 0;
    private final int ALIAS_DUP = 1;
    private final int ALIAS_EMPTY = 2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(CreateWalletViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_create_wallet_step2, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnStep2Listener) {
            mListener = (OnStep2Listener) context;
        } else {
            throw new RuntimeException(context + " must implement OnStep2Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (disposable != null
                && !disposable.isDisposed())
            disposable.dispose();

        mListener = null;
    }

    private void initView(View v) {
        scroll = v.findViewById(R.id.scroll);
        layoutButtons = v.findViewById(R.id.layout_buttons);
        inputAlias = v.findViewById(R.id.input_alias);
        inputAlias.disableCopyPaste();
        inputAlias.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {
            }

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
                    btnNext.setEnabled(false);
                }
            }
        });
        inputAlias.setOnKeyPreImeListener(onKeyPreIme);

        inputPwd = v.findViewById(R.id.input_pwd);
        inputPwd.disableCopyPaste();
        inputPwd.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {
            }

            @Override
            public void onReleased() {
                int result = PasswordValidator.validatePassword(inputPwd.getText());
                switch (result) {
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
                    btnNext.setEnabled(false);
                }
            }
        });
        inputPwd.setOnKeyPreImeListener(onKeyPreIme);

        inputCheck = v.findViewById(R.id.input_pwd_check);
        inputCheck.disableCopyPaste();
        inputCheck.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {
            }

            @Override
            public void onReleased() {
                if (inputCheck.getText().isEmpty()) {
                    btnNext.setEnabled(false);
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
                if (s.length() == 0) btnNext.setEnabled(false);
            }
        });
        inputCheck.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                setNextEnable(inputAlias.getText(), inputPwd.getText(), inputCheck.getText());
            }
        });
        inputCheck.setOnKeyPreImeListener(onKeyPreIme);

        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
        progress = v.findViewById(R.id.progress);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next:
                btnNext.setEnabled(false);
                progress.setVisibility(View.VISIBLE);

                disposable = Observable.create(new ObservableOnSubscribe<String[]>() {
                    @Override
                    public void subscribe(ObservableEmitter<String[]> emitter) throws Exception {
                        emitter.onNext(createWallet());
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<String[]>() {
                            @Override
                            public void onNext(String[] result) {
                                Wallet wallet = new Wallet();
                                wallet.setAlias(Utils.strip(inputAlias.getText()));
                                wallet.setAddress(result[0]);
                                vm.setPrivateKey(result[1]);
                                wallet.setKeyStore(result[2]);
                                wallet.setCoinType(vm.getCoin().getValue().getSymbol().toUpperCase());

                                List<WalletEntry> entries = new ArrayList<>();
                                WalletEntry coin = new WalletEntry();
                                coin.setType(MyConstants.TYPE_COIN);
                                coin.setAddress(result[0]);

                                if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                                    coin.setName(MyConstants.NAME_ICX);
                                    coin.setSymbol(Constants.KS_COINTYPE_ICX);
                                } else {
                                    coin.setName(MyConstants.NAME_ETH);
                                    coin.setSymbol(Constants.KS_COINTYPE_ETH);
                                }

                                entries.add(coin);
                                wallet.setWalletEntries(entries);

                                vm.setWallet(wallet);

                                mListener.onStep2Done();
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                                btnNext.setEnabled(true);
                                progress.setVisibility(View.GONE);
                            }
                        });
                break;

            case R.id.btn_back:
                mListener.onStep2Back();
                break;
        }
    }

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

    private void setNextEnable(String alias, String pwd, String checkPwd) {
        int aliasValidate = 0;
        int pwdValidate = 0;
        boolean matched = true;

        if (!inputAlias.toString().isEmpty()) {
            aliasValidate = checkAlias(alias);
            switch (aliasValidate) {
                case ALIAS_DUP:
                    inputAlias.setError(true, getString(R.string.duplicateWalletAlias));
                    break;
                default:
                    inputAlias.setError(false, null);
            }
        } else {
            aliasValidate = ALIAS_EMPTY;
            btnNext.setEnabled(false);
            return;
        }

        if (!inputPwd.getText().isEmpty()) {
            pwdValidate = PasswordValidator.validatePassword(pwd);
            if (pwdValidate != 0) {
                switch (pwdValidate) {
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
            pwdValidate = PasswordValidator.EMPTY;
            btnNext.setEnabled(false);
            return;
        }

        if (!inputCheck.getText().isEmpty()) {
            matched = checkPasswordMatch(pwd, checkPwd);
            if (!matched) {
                inputCheck.setError(true, getString(R.string.errPasswordNotMatched));
            } else {
                inputCheck.setError(false, null);
            }
        } else
            matched = false;


        if (aliasValidate == OK && matched && (pwdValidate == PasswordValidator.OK))
            btnNext.setEnabled(true);
        else
            btnNext.setEnabled(false);
    }

    private String[] createWallet() {
        if (vm.getCoin().getValue() == MyConstants.Coin.ICX) {
            return KeyStoreUtils.generateICXKeystore(inputPwd.getText());
        } else {
            return KeyStoreUtils.generateEtherKeystore(inputPwd.getText());
        }
    }

    private TTextInputLayout.OnKeyPreIme onKeyPreIme = new TTextInputLayout.OnKeyPreIme() {
        @Override
        public void onDone() {
            setNextEnable(inputAlias.getText(), inputPwd.getText(), inputCheck.getText());
        }
    };

    public interface OnStep2Listener {
        void onStep2Done();

        void onStep2Back();
    }
}

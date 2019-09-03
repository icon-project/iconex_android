package foundation.icon.iconex.view.ui.load;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonObject;

import java.util.List;

import foundation.icon.MyConstants;
import foundation.icon.iconex.wallet.Wallet;

public class LoadViewModel extends ViewModel {

    private MutableLiveData<LoadMethod> method;
    private MutableLiveData<MyConstants.Coin> coinType;
    private MutableLiveData<String> privateKey;

    private MutableLiveData<FileType> fileType;
    private MutableLiveData<JsonObject> keystore;
    private MutableLiveData<List<Wallet>> bundle;

    public MutableLiveData<LoadMethod> getMethod() {
        if (method == null)
            method = new MutableLiveData<>();

        return method;
    }

    public void setMethod(LoadMethod method) {
        getMethod().setValue(method);
    }

    public MutableLiveData<MyConstants.Coin> getCoin() {
        if (coinType == null)
            coinType = new MutableLiveData<>();

        return coinType;
    }

    public void setCoinType(MyConstants.Coin coin) {
        getCoin().setValue(coin);
    }

    public MutableLiveData<String> getPrivateKey() {
        if (privateKey == null)
            privateKey = new MutableLiveData<>();

        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        getPrivateKey().setValue(privateKey);
    }

    public MutableLiveData<FileType> getFileType() {
        if (fileType == null)
            fileType = new MutableLiveData<>();

        return fileType;
    }

    public void setFileType(FileType fileType) {
        getFileType().setValue(fileType);
    }

    public MutableLiveData<JsonObject> getKeystore() {
        if (keystore == null)
            keystore = new MutableLiveData<>();

        return keystore;
    }

    public void setKeystore(JsonObject keystore) {
        getKeystore().setValue(keystore);
    }

    public MutableLiveData<List<Wallet>> getBundle() {
        if (bundle == null)
            bundle = new MutableLiveData<>();

        return bundle;
    }

    public void setBundle(List<Wallet> bundle) {
        getBundle().setValue(bundle);
    }

    public enum LoadMethod {
        KEYSTORE("keystore"),
        PRIVATE_KEY("privateKey");

        private String method;

        public String getMethod() {
            return method;
        }

        LoadMethod(String method) {
            this.method = method;
        }

        public static LoadMethod fromMethod(String method) {
            if (method != null) {
                for (LoadMethod m : values()) {
                    if (m.getMethod().equals(method))
                        return m;
                }
            }

            return null;
        }
    }

    public enum FileType {
        KEYSTORE,
        BUNDLE
    }
}

package foundation.icon.iconex.service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.icx.Call;
import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.IconAmount;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcArray;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import loopchain.icon.wallet.core.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class PRepService {

    private IconService iconService;

    public PRepService(String host) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        iconService = new IconService(new HttpProvider(httpClient, host));
    }

    public RpcItem getPreps() throws IOException {
        Call<RpcItem> call = new Call.Builder()
                .to(new Address(Constants.ADDRESS_ZERO))
                .method("getPReps")
                .build();

        return iconService.call(call).execute();
    }

    public RpcItem getPrep(String prepAddress) throws IOException {
        RpcObject params = new RpcObject.Builder()
                .put("address", new RpcValue(prepAddress))
                .build();

        Call<RpcItem> call = new Call.Builder()
                .to(new Address(Constants.ADDRESS_ZERO))
                .method("getPRep")
                .params(params)
                .build();

        return iconService.call(call).execute();
    }

    public RpcItem getIScore(String address) throws IOException {
        RpcObject params = new RpcObject.Builder()
                .put("address", new RpcValue(address))
                .build();

        Call<RpcItem> call = new Call.Builder()
                .to(new Address(Constants.ADDRESS_ZERO))
                .method("queryIScore")
                .params(params)
                .build();

        return iconService.call(call).execute();
    }

    public String setStake(SignedTransaction signedTransaction) throws IOException {
        return iconService.sendTransaction(signedTransaction).execute().toHexString(true);
    }

    public RpcItem getStake(String address) throws IOException {
        RpcObject params = new RpcObject.Builder()
                .put("address", new RpcValue(address))
                .build();

        Call<RpcItem> call = new Call.Builder()
                .to(new Address(Constants.ADDRESS_ZERO))
                .method("getStake")
                .params(params)
                .build();

        return iconService.call(call).execute();
    }

    public BigInteger estimateUnstakeLockPeriod() throws IOException {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        IconService iconService = new foundation.icon.icx.IconService(new HttpProvider(httpClient, Urls.Network.Zicon.getUrlNoEndPoint(), 3));
        Call<RpcItem> call = new Call.Builder()
                .to(new Address(Constants.ADDRESS_ZERO))
                .method("estimateUnstakeLockPeriod")
                .build();

        return iconService.call(call).execute().asObject().getItem("unstakeLockPeriod").asInteger();
    }

    public RpcItem getDelegation(String address) throws IOException {
        RpcObject params = new RpcObject.Builder()
                .put("address", new RpcValue(address))
                .build();

        Call<RpcItem> call = new Call.Builder()
                .to(new Address(Constants.ADDRESS_ZERO))
                .method("getDelegation")
                .params(params)
                .build();

        return iconService.call(call).execute();
    }

    public String setDelegation(KeyWallet keyWallet, List<Delegation> delegations, BigInteger stepLimit) throws IOException {
        RpcArray.Builder arrayBuilder = new RpcArray.Builder();
        for (Delegation d : delegations) {
            BigInteger value;
            if (d.isEdited())
                value = d.getEdited().toBigInteger();
            else
                value = d.getValue();

            RpcObject object = new RpcObject.Builder()
                    .put("address", new RpcValue(d.getPrep().getAddress()))
                    .put("value", new RpcValue(ConvertUtil.valueToHexString(ConvertUtil.getValue(value, 18), 18)))
                    .build();
            arrayBuilder.add(object);
        }

        RpcObject params = new RpcObject.Builder()
                .put("delegations", arrayBuilder.build())
                .build();

        Transaction transaction = TransactionBuilder.newBuilder()
                .from(keyWallet.getAddress())
                .to(new Address(Constants.ADDRESS_ZERO))
                .value(IconAmount.of("0", IconAmount.Unit.ICX).toLoop())
                .stepLimit(stepLimit)
                .nid(ICONexApp.NETWORK.getNid())
                .call("setDelegation")
                .params(params)
                .build();

        return iconService.sendTransaction(new SignedTransaction(transaction, keyWallet)).execute().toHexString(true);
    }

    public String claimIScore(SignedTransaction signedTransaction) throws IOException {
        return iconService.sendTransaction(signedTransaction).execute().toHexString(true);
    }
}

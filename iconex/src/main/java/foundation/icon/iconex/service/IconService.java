package foundation.icon.iconex.service;

import java.io.IOException;
import java.math.BigInteger;

import foundation.icon.ICONexApp;
import foundation.icon.icx.Call;
import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import loopchain.icon.wallet.core.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class IconService {

    private foundation.icon.icx.IconService iconService;

    public IconService(String host) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        iconService = new foundation.icon.icx.IconService(new HttpProvider(httpClient, host));
    }

    public BigInteger getBalance(String address) throws IOException {
        return iconService.getBalance(new Address(address)).execute();
    }

    public Bytes sendTransaction(SignedTransaction transaction) throws IOException {
        return iconService.sendTransaction(transaction).execute();
    }

    public RpcItem getStepPrice() throws IOException {
        Call<RpcItem> call = new Call.Builder()
                .to(new Address(Constants.ADDRESS_GOVERNANCE))
                .method(Constants.METHOD_GETSTEPPRICE)
                .build();

        return iconService.call(call).execute();
    }

    public static BigInteger estimateStep(Transaction transaction) throws IOException {
        foundation.icon.icx.IconService estimated =
                new foundation.icon.icx.IconService(new HttpProvider(ICONexApp.NETWORK.getUrlNoEndPoint(), 3));

        return estimated.estimateStep(transaction).execute();
    }
}

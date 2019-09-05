package foundation.icon.iconex.service;

import com.google.gson.Gson;

import java.io.IOException;

import foundation.icon.icx.Call;
import foundation.icon.icx.IconService;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class PRepService {

    private IconService iconService;
    private Gson gson = new Gson();

    public PRepService(String host) throws Exception {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        iconService = new IconService(new HttpProvider(httpClient, host));
    }

    public RpcItem getPreps() throws IOException {
        Call<RpcItem> call = new Call.Builder()
                .to(new Address("cx0000000000000000000000000000000000000000"))
                .method("getPReps")
                .build();

        return iconService.call(call).execute();
    }

    public RpcItem getPrep(String prepAddress) throws IOException {
        RpcObject params = new RpcObject.Builder()
                .put("address", new RpcValue(prepAddress))
                .build();

        Call<RpcItem> call = new Call.Builder()
                .to(new Address("cx0000000000000000000000000000000000000000"))
                .method("getPRep")
                .params(params)
                .build();

        return iconService.call(call).execute();
    }
}

package foundation.icon.iconex.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;

import foundation.icon.iconex.service.response.VSResponse;
import loopchain.icon.wallet.core.request.RequestData;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.core.response.TRResponse;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RESTClient {

    private RESTApiService RESTService;
    private Gson gson = new Gson();

    public RESTClient(String host) throws Exception {

        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {

                okhttp3.Request original = chain.request();

                okhttp3.Request request = original.newBuilder()
//                        .header("Content-Type", "application/json; charset=utf-8")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        }).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RESTService = retrofit.create(RESTApiService.class);
    }

    public Call<LCResponse> sendRequest(RequestData reqData) throws IOException {
        Call<LCResponse> response = RESTService.sendRequest(reqData);
        return response;
    }

    public Call<TRResponse> sendGetExRates(String reqData) throws IOException {
        Call<TRResponse> response = RESTService.sendGetExchangeList(reqData);
        return response;
    }

    public Call<TRResponse> sendGetTxList(String address, int page) throws IOException {
        Call<TRResponse> response = RESTService.sendGetTxList(address, page);
        return response;
    }

    public Call<VSResponse> sendVersionCheck() throws IOException {
        Call<VSResponse> response = RESTService.sendVersionCheck();
        return response;
    }

    public Call<JsonElement> getEthTokens() throws IOException {
        Call<JsonElement> response = RESTService.getEthTokens();
        return response;
    }
}

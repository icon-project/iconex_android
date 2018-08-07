package foundation.icon.iconex.service;

import com.google.gson.JsonElement;

import foundation.icon.iconex.service.response.VSResponse;
import loopchain.icon.wallet.core.request.RequestData;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.core.response.TRResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by js on 2018. 2. 15..
 */

public interface RESTApiService {

    @POST(ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_VERSION)
    Call<LCResponse> sendRequest(@Body RequestData requestData);

    @GET(ServiceConstants.TR_API_VERSION + ServiceConstants.TR_API_EX_HEADER)
    Call<TRResponse> sendGetExchangeList(@Query("codeList") String codeList);

    @GET(ServiceConstants.TR_API_VERSION + ServiceConstants.TR_API_TX_LIST_HEADER)
    Call<TRResponse> sendGetTxList(@Query("address") String address, @Query("page") int page);

    @GET(ServiceConstants.VS_API)
    Call<VSResponse> sendVersionCheck();

    @GET(ServiceConstants.ETH_TOKENS)
    Call<JsonElement> getEthTokens();
}

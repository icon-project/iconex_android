package foundation.icon.iconex.service;

import foundation.icon.icx.transport.jsonrpc.RpcItem;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PrepApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST(Urls.endPoint)
    Call<RpcItem> getPreps(@Body RpcItem request);

    @POST(Urls.endPoint)
    Call<RpcItem> getPrep(@Body RpcItem request);
}

package foundation.icon.connect;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.icx.Call;
import foundation.icon.icx.Callback;
import foundation.icon.icx.IconService;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.ScoreApi;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import static foundation.icon.ICONexApp.network;

public class IconexConnect {
    private static final String TAG = IconexConnect.class.getSimpleName();

    private final Activity parent;
    private IconService iconService;
    private RequestParser parser;
    private final RequestData request;

    private Constants.Method method;
    private Wallet boundWallet;
    private String toAddress;
    private String symbol;
    private int decimals;
    private String contractAddress;

    private BigInteger stepPrice;
    private BigInteger defaultLimit;
    private BigInteger contractCall;
    private BigInteger inputLimit;
    private BigInteger maxLimit;
    private BigInteger txFee;

    private BigInteger icxBalance;

    public IconexConnect(Activity parent, RequestData request) {
        this.parent = parent;
        this.request = request;
        parser = RequestParser.newInstance(parent);
    }

    public void startConnectActivity() {
        try {
            parser.requestValidate(request);
            method = parser.getMethod(request.getData());
            int id = parser.getId(request.getData());

            if (method == Constants.Method.BIND) {
                parent.startActivity(new Intent(parent, SelectWalletActivity.class)
                        .putExtra("id", id)
                        .putExtra("request", request));
                parent.finish();
            } else {
                String address;
                try {
                    address = parser.validateParameters(method, parser.getParams(parser.getData(request.getData())));
                } catch (ErrorCodes.Error e) {
                    sendError(parent, request, e);
                    parent.finishAffinity();
                    return;
                }

                boundWallet = findWallet(address);
                if (boundWallet == null)
                    throw new ErrorCodes.Error(ErrorCodes.ERR_NO_WALLET, String.format(Locale.getDefault(), ErrorCodes.MSG_NO_WALLET, address));
                else {
                    if (method != Constants.Method.SIGN) {
                        try {
                            JSONObject params = parser.getParams(parser.getData(request.getData()));
                            toAddress = params.getString("to");

                            if (boundWallet.getAddress().equals(toAddress))
                                sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_SAME_ADDRESS, "Same address"));
                            else if (toAddress.startsWith("hx")
                                    || toAddress.startsWith("cx")) {
                                if (toAddress.substring(2).length() < 40)
                                    sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INVALID_PARAMETER,
                                            String.format(Locale.getDefault(), ErrorCodes.MSG_INVALID_PARAM, address)));
                            } else
                                sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INVALID_PARAMETER,
                                        String.format(Locale.getDefault(), ErrorCodes.MSG_INVALID_PARAM, address)));

                        } catch (JSONException e) {

                        }

                        if (method == Constants.Method.SendICX) {
                            decimals = 18;
                            symbol = "ICX";
                            getStepPrice(method);
                        } else {
                            try {
                                contractAddress = parser.getParams(parser.getData(request.getData())).getString("contractAddress");
                            } catch (JSONException e) {

                            }
                            getTokenInfo();
                        }
                    } else {
                        parent.startActivity(new Intent(parent, EnterPasswordActivity.class)
                                .putExtra("id", id)
                                .putExtra("method", method)
                                .putExtra("request", request)
                                .putExtra("wallet", (Serializable) boundWallet));
                        parent.finish();
                    }
                }
            }
        } catch (ErrorCodes.Error e) {
            if (e.getCode() == ErrorCodes.ERR_NOT_FOUND_CALLER) {
                Toast.makeText(parent, e.getCode() + " : " + e.getResult(), Toast.LENGTH_SHORT).show();
                ICONexApp.isConnect = false;
                parent.finishAffinity();
            } else
                sendError(parent, request, e);
        }
    }

    private Wallet findWallet(String address) {
        for (Wallet wallet : ICONexApp.wallets) {
            if (wallet.getAddress().equals(address))
                return wallet;
        }

        return null;
    }

    private void getStepPrice(final Constants.Method method) {
        initIconService();
        final Address scoreAddress = new Address("cx0000000000000000000000000000000000000001");

        Call<RpcItem> call = new Call.Builder()
                .to(scoreAddress)
                .method("getStepPrice")
                .build();

        iconService.call(call).execute(new Callback<RpcItem>() {
            @Override
            public void onSuccess(RpcItem result) {
                try {
                    stepPrice = ConvertUtil.hexStringToBigInt(result.toString(), 18);
                } catch (Exception e) {

                }
                getStepCosts(method);
            }

            @Override
            public void onFailure(Exception exception) {
                sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_NETWORK,
                        String.format(Locale.getDefault(), ErrorCodes.MSG_NETWORK, exception.getMessage())));
            }
        });
    }

    private void getStepCosts(Constants.Method method) {
        initIconService();

        final Address scoreAddress = new Address("cx0000000000000000000000000000000000000001");

        Call<RpcItem> call = new Call.Builder()
                .to(scoreAddress)
                .method("getStepCosts")
                .build();

        iconService.call(call).execute(new Callback<RpcItem>() {
            @Override
            public void onSuccess(RpcItem result) {
                RpcObject object = result.asObject();
                defaultLimit = object.getItem("default").asInteger();
                contractCall = object.getItem("contractCall").asInteger();
                inputLimit = object.getItem("input").asInteger();

                if (method == Constants.Method.SendICX)
                    txFee = stepPrice.multiply(defaultLimit);
                else
                    txFee = stepPrice.multiply(defaultLimit.multiply(BigInteger.valueOf(2)));

                getMaxStepLimit();
            }

            @Override
            public void onFailure(Exception exception) {
                sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_NETWORK,
                        String.format(Locale.getDefault(), ErrorCodes.MSG_NETWORK, exception.getMessage())));
            }
        });
    }

    private void getMaxStepLimit() {
        initIconService();

        final Address scoreAddress = new Address("cx0000000000000000000000000000000000000001");

        RpcObject params = new RpcObject.Builder()
                .put("contextType", new RpcValue("invoke"))
                .build();

        Call<RpcItem> call = new Call.Builder()
                .to(scoreAddress)
                .method("getMaxStepLimit")
                .params(params)
                .build();

        iconService.call(call).execute(new Callback<RpcItem>() {
            @Override
            public void onSuccess(RpcItem result) {
                try {
                    maxLimit = result.asInteger();

                    getBalance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception exception) {
                sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_NETWORK,
                        String.format(Locale.getDefault(), ErrorCodes.MSG_NETWORK, exception.getMessage())));
            }
        });
    }

    private void getBalance() {
        initIconService();
        Address address;

        address = new Address(boundWallet.getAddress());
        iconService.getBalance(address).execute(icxCallback);
    }

    private void getTokenInfo() {
        initIconService();

        Address scoreAddress = new Address(contractAddress);
        iconService.getScoreApi(scoreAddress).execute(new Callback<List<ScoreApi>>() {
            @Override
            public void onSuccess(List<ScoreApi> result) {
                Call<RpcItem> call = new Call.Builder()
                        .from(new Address(boundWallet.getAddress()))
                        .to(scoreAddress)
                        .method("symbol")
                        .build();

                iconService.call(call).execute(new Callback<RpcItem>() {
                    @Override
                    public void onSuccess(RpcItem result) {
                        symbol = result.toString();
                        Call<RpcItem> call = new Call.Builder()
                                .from(new Address(boundWallet.getAddress()))
                                .to(scoreAddress)
                                .method("decimals")
                                .build();

                        iconService.call(call).execute(new Callback<RpcItem>() {
                            @Override
                            public void onSuccess(RpcItem result) {
                                decimals = result.asInteger().intValue();
                                getStepPrice(method);
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_NETWORK,
                                        String.format(Locale.getDefault(), ErrorCodes.MSG_NETWORK, exception.getMessage())));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_NETWORK,
                                String.format(Locale.getDefault(), ErrorCodes.MSG_NETWORK, exception.getMessage())));
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INVALID_PARAMETER,
                        String.format(Locale.getDefault(), ErrorCodes.MSG_INVALID_PARAM, "contractAddress")));
            }
        });
    }

    private void getTokenBalance() {
        initIconService();

        Address fromAddress = new Address(boundWallet.getAddress());
        Address scoreAddress = new Address(contractAddress);

        RpcObject params = new RpcObject.Builder()
                .put("_owner", new RpcValue(fromAddress))
                .build();

        Call<RpcItem> call = new Call.Builder()
                .from(fromAddress)
                .to(scoreAddress)
                .method("balanceOf")
                .params(params)
                .build();

        iconService.call(call).execute(new Callback<RpcItem>() {
            @Override
            public void onSuccess(RpcItem result) {
                BigInteger balance = result.asInteger();
                BigInteger amount = BigInteger.ZERO;
                try {
                    String value = parser.getParams(parser.getData(request.getData())).getString("value");
                    amount = ConvertUtil.hexStringToBigInt(value, decimals);

                    if (balance.compareTo(BigInteger.ZERO) == 0)
                        sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INSUFFICIENT_BALANCE, String.format(ErrorCodes.MSG_INSUFFICIENT)));
                    else if (balance.compareTo(amount) < 0)
                        sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INSUFFICIENT_BALANCE, String.format(ErrorCodes.MSG_INSUFFICIENT)));
                    else {
                        for (int i = 0; i < boundWallet.getWalletEntries().size(); i++) {
                            WalletEntry entry = boundWallet.getWalletEntries().get(i);
                            if (entry.getContractAddress().equals(contractAddress)) {
                                TransactionData transactionData = new TransactionData.Builder()
                                        .alias(boundWallet.getAlias())
                                        .stepPrice(stepPrice)
                                        .defaultLimit(defaultLimit)
                                        .contractCall(contractCall)
                                        .input(inputLimit)
                                        .maxLimit(maxLimit)
                                        .decimals(decimals)
                                        .symbol(symbol)
                                        .balance(icxBalance)
                                        .tokenBalance(result.asInteger())
                                        .build();

                                parent.startActivity(new Intent(parent, EnterPasswordActivity.class)
                                        .putExtra("id", parser.getId(request.getData()))
                                        .putExtra("method", method)
                                        .putExtra("request", request)
                                        .putExtra("wallet", (Serializable) boundWallet)
                                        .putExtra("txData", transactionData));

                                parent.finish();
                            }
                        }
                    }
                } catch (Exception e) {
                    sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INVALID_PARAMETER, "value"));
                    parent.finishAffinity();
                }
            }

            @Override
            public void onFailure(Exception exception) {
                sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_NETWORK,
                        String.format(Locale.getDefault(), ErrorCodes.MSG_NETWORK, exception.getMessage())));
            }
        });
    }

    private Callback<BigInteger> icxCallback = new Callback<BigInteger>() {
        @Override
        public void onSuccess(BigInteger result) {
            icxBalance = result;
            BigInteger amount = BigInteger.ZERO;
            try {
                String value = parser.getParams(parser.getData(request.getData())).getString("value");
                amount = ConvertUtil.hexStringToBigInt(value, decimals);

                if (icxBalance.compareTo(BigInteger.ZERO) == 0)
                    sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INSUFFICIENT_BALANCE, ErrorCodes.MSG_INSUFFICIENT));
                else if (icxBalance.compareTo(txFee) < 0)
                    sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INSUFFICIENT_BALANCE_FOR_FEE, ErrorCodes.MSG_INSUFFICIENT_FEE));
                else if (icxBalance.compareTo(amount.add(txFee)) < 0)
                    sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INSUFFICIENT_BALANCE_FOR_FEE, ErrorCodes.MSG_INSUFFICIENT_FEE));
                else if (method == Constants.Method.SendToken)
                    getTokenBalance();
                else {
                    TransactionData transactionData = new TransactionData.Builder()
                            .alias(boundWallet.getAlias())
                            .stepPrice(stepPrice)
                            .defaultLimit(defaultLimit)
                            .contractCall(contractCall)
                            .input(inputLimit)
                            .maxLimit(maxLimit)
                            .decimals(decimals)
                            .symbol(symbol)
                            .balance(icxBalance)
                            .build();

                    parent.startActivity(new Intent(parent, EnterPasswordActivity.class)
                            .putExtra("id", parser.getId(request.getData()))
                            .putExtra("method", method)
                            .putExtra("request", request)
                            .putExtra("wallet", (Serializable) boundWallet)
                            .putExtra("txData", transactionData));

                    parent.finish();
                }
            } catch (Exception e) {
                sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_INVALID_PARAMETER,
                        String.format(Locale.getDefault(), ErrorCodes.MSG_INVALID_PARAM, "value")));
                parent.finishAffinity();
            }
        }

        @Override
        public void onFailure(Exception exception) {
            sendError(parent, request, new ErrorCodes.Error(ErrorCodes.ERR_NETWORK,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NETWORK, exception.getMessage())));
        }
    };

    private void initIconService() {
        if (iconService == null) {
            String url = null;
            switch (network) {
                case MyConstants.NETWORK_MAIN:
                    url = ServiceConstants.TRUSTED_HOST_MAIN + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;

                case MyConstants.NETWORK_TEST:
                    url = ServiceConstants.TRUSTED_HOST_TEST + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;

                case MyConstants.NETWORK_DEV:
                    url = ServiceConstants.DEV_HOST + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;
            }

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            iconService = new IconService(new HttpProvider(httpClient, url));
        }
    }

    public static void sendResponse(Activity activity, RequestData request, String result) {
        RequestParser parser = RequestParser.newInstance(activity);
        Intent intent = new Intent()
                .setClassName(request.getCaller(), request.getReceiver())
                .setAction(foundation.icon.connect.Constants.C_ACTION)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        ResponseData resData = new ResponseData(parser.getId(request.getData()), Constants.SUCCESS, result);
        intent.putExtra("data", resData.getResponse());

        ICONexApp.isConnect = false;
        ICONexApp.connectMethod = Constants.Method.NONE;
        activity.sendBroadcast(intent);

        activity.finishAffinity();
    }

    public static void sendError(Activity activity, RequestData request, ErrorCodes.Error e) {
        RequestParser parser = RequestParser.newInstance(activity);
        Intent intent = new Intent().setClassName(request.getCaller(), request.getReceiver())
                .setAction(Constants.C_ACTION)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        ResponseData response = new ResponseData(parser.getId(request.getData()), e.getCode(), e.getResult());
        intent.putExtra("data", response.getResponse());

        ICONexApp.isConnect = false;
        ICONexApp.connectMethod = Constants.Method.NONE;
        activity.sendBroadcast(intent);

        activity.finishAffinity();
    }
}

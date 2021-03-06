package foundation.icon.connect;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;

import foundation.icon.ICONexApp;

public class RequestParser {
    private static final String TAG = RequestParser.class.getSimpleName();

    private final Context mContext;
    public static final int SUCCESS = 0;

    public RequestParser(Context context) {
        mContext = context;
    }

    public static RequestParser newInstance(Context context) {
        RequestParser rp = new RequestParser(context);
        return rp;
    }

    public int requestValidate(RequestData request) throws ErrorCodes.Error {
        String data = request.getData();
        String caller = request.getCaller();
        String receiver = request.getReceiver();

        if (caller == null || caller.isEmpty())
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND_CALLER, ErrorCodes.MSG_NOT_FOUND_CALLER);

        checkCaller(caller, receiver);

        if (receiver == null || receiver.isEmpty())
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND_CALLER, ErrorCodes.MSG_NOT_FOUND_CALLER);

        if (data == null || data.isEmpty())
            throw new ErrorCodes.Error(ErrorCodes.ERR_PARSE, ErrorCodes.MSG_PARSE);

        return SUCCESS;
    }

    public Constants.Method getMethod(String data) throws ErrorCodes.Error {
        JSONObject requestData = getData(data);

        if (requestData == null)
            throw new ErrorCodes.Error(ErrorCodes.ERR_PARSE, ErrorCodes.MSG_PARSE);

        try {
            String method = requestData.getString("method");
            switch (method) {
                case "bind":
                    ICONexApp.connectMethod = Constants.Method.BIND;
                    return Constants.Method.BIND;

                case "sign":
                    ICONexApp.connectMethod = Constants.Method.SIGN;
                    return Constants.Method.SIGN;

                case "sendICX":
                    ICONexApp.connectMethod = Constants.Method.SendICX;
                    return Constants.Method.SendICX;

                case "sendToken":
                    ICONexApp.connectMethod = Constants.Method.SendToken;
                    return Constants.Method.SendToken;
                default:
                    ICONexApp.connectMethod = Constants.Method.NONE;
            }

            throw new ErrorCodes.Error(ErrorCodes.ERR_INVALID_METHOD, ErrorCodes.MSG_INVALID_M);
        } catch (JSONException e) {
            ICONexApp.connectMethod = Constants.Method.NONE;
            throw new ErrorCodes.Error(ErrorCodes.ERR_PARSE, ErrorCodes.MSG_PARSE);
        }
    }

    public String validateParameters(Constants.Method method, JSONObject params) throws ErrorCodes.Error {
        String address = null;
        switch (method) {
            case SIGN:
                address = valSignParams(params);
                break;

            case SendICX:
                address = valSendICXParams(params);
                break;

            case SendToken:
                address = valSendTokenParams(params);
                break;
        }

        return address;
    }

    private String valSignParams(JSONObject params) throws ErrorCodes.Error {
        String address;
        if (!params.has("version") || params.isNull("version"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "version"));

        if (!params.has("from") || params.isNull("from"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "from"));

        if (!params.has("to") || params.isNull("to"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "to"));

        if (!params.has("value") || params.isNull("value"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "value"));

        if (!params.has("stepLimit") || params.isNull("stepLimit"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "stepLimit"));

        if (!params.has("timestamp") || params.isNull("timestamp"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "timestamp"));

        if (!params.has("nid") || params.isNull("nid"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "nid"));

        if (!params.has("nonce") || params.isNull("nonce"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "nonce"));

        try {
            address = params.getString("from");
        } catch (JSONException e) {
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "from"));
        }

        return address;
    }

    private String valSendICXParams(JSONObject params) throws ErrorCodes.Error {
        String address;

        if (!params.has("from") || params.isNull("from"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "from"));

        if (!params.has("to") || params.isNull("to"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "to"));

        if (!params.has("value") || params.isNull("value"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "value"));

        if (params.has("dataType") && !params.isNull("dataType")) {
            String dataType = null;
            try {
                dataType = params.getString("dataType");
            } catch (JSONException e) {
                // To do nothing.
            }

            if (dataType != null && !dataType.isEmpty()) {
                try {
                    String data = params.getString("data");
                    if (data.isEmpty())
                        throw new JSONException("empty");
                } catch (JSONException e) {
                    throw new ErrorCodes.Error(ErrorCodes.ERR_PARSE, ErrorCodes.MSG_PARSE);
                }
            }
        }

        if (params.has("data") && !params.isNull("data")) {
            String data = null;
            try {
                data = params.getString("data");
            } catch (JSONException e) {
                // To do nothing.
            }

            if (data != null && !data.isEmpty()) {
                try {
                    String dataType = params.getString("dataType");
                    if (dataType.isEmpty())
                        throw new JSONException("empty");
                } catch (JSONException e) {
                    throw new ErrorCodes.Error(ErrorCodes.ERR_PARSE, ErrorCodes.MSG_PARSE);
                }
            }
        }

        try {
            address = params.getString("from");
        } catch (JSONException e) {
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "from"));
        }

        return address;
    }

    private String valSendTokenParams(JSONObject params) throws ErrorCodes.Error {
        String address;

        if (!params.has("from") || params.isNull("from"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "from"));

        if (!params.has("to") || params.isNull("to"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "to"));

        if (!params.has("value") || params.isNull("value"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "value"));

        if (!params.has("contractAddress") || params.isNull("contractAddress"))
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "contractAddress"));
        try {
            address = params.getString("from");
        } catch (JSONException e) {
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND,
                    String.format(Locale.getDefault(), ErrorCodes.MSG_NOT_FOUND, "from"));
        }

        return address;
    }

    public int getId(String data) {
        JSONObject jsonData = getData(data);
        try {
            return jsonData.getInt("id");
        } catch (JSONException e) {
            return -1;
        }
    }

    public JSONObject getData(String data) {
        byte[] base64Data = Base64.decode(data, Base64.NO_WRAP);
        JSONObject jsonData = null;
        try {
            jsonData = new JSONObject(new String(base64Data));
        } catch (JSONException e) {
            return jsonData;
        }

        return jsonData;
    }

    public JSONObject getParams(JSONObject data) {
        JSONObject jsonData;
        try {
            jsonData = data.getJSONObject("params");
        } catch (JSONException e) {
            try {
                JsonObject gson = new JsonParser().parse(data.getString("params")).getAsJsonObject();
                jsonData = new JSONObject(gson.toString());
            } catch (JSONException e1) {
                return null;
            }
        }

        return jsonData;
    }

    private int checkCaller(String caller, String receiver) throws ErrorCodes.Error {
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(caller, PackageManager.GET_RECEIVERS);
            for (ActivityInfo resReceiver : info.receivers) {
                if (resReceiver.name.equals(receiver))
                    return SUCCESS;
            }

            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND_CALLER, ErrorCodes.MSG_NOT_FOUND_CALLER);
        } catch (PackageManager.NameNotFoundException e) {
            throw new ErrorCodes.Error(ErrorCodes.ERR_NOT_FOUND_CALLER, ErrorCodes.MSG_NOT_FOUND_CALLER);
        }
    }

    public String paramsToString(JSONObject requestData) {
        StringBuilder sb = new StringBuilder();

        try {
            JSONObject params = getParams(requestData);
            Iterator<?> keys = params.keys();
            String key;
            sb.append("{").append("\n");
            while (keys.hasNext()) {
                key = (String) keys.next();
                if (!key.equals("data") && !key.equals("dataType")) {
                    sb.append("\t\"" + key + "\": "
                            + "\"" + params.getString(key) + "\"\n");
                } else {
                    sb.append(dataToString(requestData));
                }
            }
            sb.append("}");
        } catch (JSONException e) {
            return "";
        }

        return sb.toString();
    }

    public String dataToString(JSONObject requestData) {
        StringBuilder sb = new StringBuilder();

        try {
            JSONObject params = getParams(requestData);

            String dataType = params.getString("dataType");
            String data = params.getString("data");

            sb.append("\t\"dataType\": \"" + dataType + "\"\n");

            if (data.startsWith("0x")) {
                sb.append("\t\"data\": \"" + data + "\"\n");
            } else {
                JSONObject dataObj;
                try {
                    dataObj = new JSONObject(data);
                } catch (JSONException e) {
                    try {
                        JsonObject gson = new JsonParser().parse(data).getAsJsonObject();
                        dataObj = new JSONObject(gson.toString());
                    } catch (Exception e1) {
                        return "";
                    }
                }

                Iterator<?> keys = dataObj.keys();

                sb.append("\t\"data\": {\n");
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (!key.equals("params")) {
                        sb.append("\t\t\"" + key + "\": \"" + dataObj.getString(key) + "\"");
                        if (dataObj.keys().hasNext())
                            sb.append(",\n");
                        else
                            sb.append("\n");
                    } else {
                        JSONObject dataParams = dataObj.getJSONObject("params");
                        Iterator<?> paramKeys = dataParams.keys();

                        sb.append("\t\t\"params\": {");
                        while (paramKeys.hasNext()) {
                            String paramKey = (String) paramKeys.next();
                            sb.append("\t\t\t\"" + paramKey + "\": \"" +
                                    dataParams.getString(paramKey) + "\"");

                            if (dataObj.keys().hasNext())
                                sb.append(",\n");
                            else
                                sb.append("\n");
                        }
                        sb.append("\t\t}\n");
                    }
                }
                sb.append("\t}\n");
            }
        } catch (JSONException e) {
            return "";
        }

        return sb.toString();
    }
}

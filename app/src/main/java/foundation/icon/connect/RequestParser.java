package foundation.icon.connect;

import android.content.Intent;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import foundation.icon.iconex.MyConstants;

public class RequestParser {

    public static final int SUCCESS = 0;

    public int requestValidate(Intent request) {
        String data = request.getStringExtra("data");
        String from = request.getStringExtra("from");
        String receiver = request.getStringExtra("receiver");

        if (from == null || from.isEmpty())
            return -1;

        if (receiver == null || receiver.isEmpty())
            return -1;

        if (data == null || data.isEmpty())
            return -1;

        return SUCCESS;
    }

//    private int validateData(String data) {
//        byte[] dataBytes = Base64.decode(data, Base64.NO_WRAP);
//        JSONObject dataObj;
//
//        try {
//            dataObj = new JSONObject(new String(dataBytes));
//        } catch (JSONException e) {
//            return ErrorCodes.ERR_PARSER;
//        }
//
//
//    }
//
//    private MyConstants.ConnectMethod getMethod(String data) {
//        JSONObject request;
//
//        try {
//            request = new JSONObject(new String(Base64.decode(data, Base64.NO_WRAP)));
//        } catch (Exception e) {
//            return MyConstants.ConnectMethod.NONE;
//        }
//    }
}

# ICONex Connect for Android
ICONex Connect is a simple protocol for supporting 3rd party applications who want send transactions ICONex wallet.

## Features
* Get address of ICON wallet which managed by ICONex.
* Request send transaction.

## Basic Transmission Protocol
* Request

| Key | Type | Description |
| --- | ---- | ----------- |
| data | String | Base64 encoded string of JSON-RPC |

```Java
PackageManager pm = this.getContext().getPackageManager();
  
try
{
    // Check ICONex app is installed.
    pm.getPackageInfo("foundation.icon.iconex", PackageManager.GET_ACTIVITIES);
    // Open ICONex
    String data = Base64 encoded string of JSON Object
    Uri url = Uri.parse("iconex://"+command+"?data="+data);
    startActivityForResult(context,new Intent(Intent.ACTION_VIEW, url), REQUEST_CODE);
}
catch (PackageManager.NameNotFoundException e)
{
    // ICONex not installed on the device.
    // Open Google playstore
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=foundation.icon.iconex")));
}
```

* Response

| Key | Type | Description |
| --- | ---- | ----------- |
| data | String | Base64 encoded string of JSON-RPC |

```Java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) 
{
    if (requestCode == 1000) {
            if (resultCode < 0) {
                Uri uri = data.getData();
                JsonObject response = new Gson().fromJson(uri.toString(), JsonObject.class);
                String message = response.get("message").getAsString();

                Toast.makeText(this, "Get response:" + resultCode + " : " + message, Toast.LENGTH_SHORT).show();
            } else {
                Uri uri = data.getData();
                JsonObject response = new Gson().fromJson(uri.toString(), JsonObject.class);
                String result = response.get("result").getAsString();

                SampleApp.from = result;
                txtAddress.setText(result);
            }
    }}
```

## JSON-RPC Speicifications
* [JSON-RPC](https://github.com/icon-project/icon-rpc-server/blob/master/docs/icon-json-rpc-v3.md#icx_sendtransaction)

## API Convention
```Java
// Response
{
    "code": $INT1,  // if code == 0 success, else fail
    "message": $STRING1,    
    "result": "$STRING2"
}
```

## Methods
| Method | Description | Required Parameters |
| ------ | ----------- | ------------------- |
| bind | Request wallet address | - |
| JSON-RPC | Request sign for transaction | version, from, value, stepLimit, timestamp, dataType(optional), data(optional) |

### bind
* Return selected wallet's address.

#### Parameters
NULL

#### Returns
Selected wallet's address.

#### Example

```Java
//Request
    Uri url = Uri.parse("iconex://bind");
    startActivityForResult(context,new Intent(Intent.ACTION_VIEW, url), REQUEST_CODE);

// Response - success
{
    "code": 1,
    "message": "success",
    "result": "hx1234..."
}

//Response - fail
{
    "code": -1000,
    "message": "Operation canceled by user."
}
```

### JSON-RPC
* Return transaction hash.

#### Parameters
JSON-RPC String

#### Returns
Transaction hash

#### Example

```Java
//Request
    String data = "{"jsonrpc": "2.0", "method": "icx_sendTransaction", "id": 1234, "params": {"version": "0x3", "from":                     "hxbe258ceb872e08851f1f59694dac2558708ece11", "to": "hx5bfdb090f43a808005ffc27c25b213145e80b7cd", "value":                     "0xde0b6b3a7640000", "timestamp": "0x563a6cf330136", "nid": "0x3", "nonce": "0x1"}}"
    String requestData = Base64.encodeToString(data.getBytes(), Base64.NO_WRAP);
    Uri url = Uri.parse("iconex://JSON-RPC?data=" + requestData);
    startActivityForResult(context,new Intent(Intent.ACTION_VIEW, url), REQUEST_CODE);

// Response - success
{
    "code": 1,
    "message": "success",
    "result": "0x1234..."
}

//Response - fail
{
    "code": -1000,
    "message": "Operation canceled by user."
}
```



## Code

| Code | Message |
| ---- | ------- |
| 0 | Success | 
| -1 | User canceled | 
| -1000 | Command not found |
| -1001 | Invalid request Could not find data |
| -1002 | Invalid method | Invalid method |
| -1003 | Invalid base64 encoded string |
| -1004 | Invalid JSON syntax |
| -2000 | Have no wallet |
| -3000 | Not found wallet($walletAddress) |
| -9999 | Unspecified error |

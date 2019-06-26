package foundation.icon.connect;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;

import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.transport.jsonrpc.RpcArray;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;

public class RequestDeserializer {

    public static Transaction deserialize(JsonParser p) throws IOException, JsonProcessingException {
        TreeNode node = p.readValueAsTree();
        return deserializer(node);
    }

    private static Transaction deserializer(TreeNode node) {

        RpcObject item = deserialize(node).asObject();

        if (item.getItem("dataType") == null) {
            return TransactionBuilder.newBuilder()
                    .nid(item.getItem("nid").asInteger())
                    .from(item.getItem("from").asAddress())
                    .to(item.getItem("to").asAddress())
                    .value(item.getItem("value").asInteger())
                    .timestamp(item.getItem("timestamp").asInteger())
                    .nonce(item.getItem("nonce").asInteger())
                    .build();
        } else {
            String dataType = item.getItem("dataType").asString();

            if (dataType.equalsIgnoreCase("call")) {
                return TransactionBuilder.newBuilder()
                        .nid(item.getItem("nid").asInteger())
                        .from(item.getItem("from").asAddress())
                        .to(item.getItem("to").asAddress())
                        .timestamp(item.getItem("timestamp").asInteger())
                        .nonce(item.getItem("nonce").asInteger())
                        .call(item.getItem("data").asObject().getItem("method").asString())
                        .params(item.getItem("data").asObject().getItem("params").asObject())
                        .build();
            } else if (dataType.equalsIgnoreCase("message")) {
                return TransactionBuilder.newBuilder()
                        .nid(item.getItem("nid").asInteger())
                        .from(item.getItem("from").asAddress())
                        .to(item.getItem("to").asAddress())
                        .value(item.getItem("value").asInteger())
                        .timestamp(item.getItem("timestamp").asInteger())
                        .nonce(item.getItem("nonce").asInteger())
                        .message(item.getItem("message").asString())
                        .build();
            } else {
                return TransactionBuilder.newBuilder()
                        .nid(item.getItem("nid").asInteger())
                        .from(item.getItem("from").asAddress())
                        .to(item.getItem("to").asAddress())
                        .value(item.getItem("value").asInteger())
                        .timestamp(item.getItem("timestamp").asInteger())
                        .nonce(item.getItem("nonce").asInteger())
                        .deploy(item.getItem("data").asObject().getItem("contentType").asString(),
                                item.getItem("data").asObject().getItem("content").asByteArray())
                        .params(item.getItem("data").asObject().getItem("params").asObject())
                        .build();
            }
        }
    }

    private static RpcItem deserialize(TreeNode node) {
        if (node.isObject()) {
            RpcObject.Builder builder = new RpcObject.Builder();
            for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                TreeNode childNode = node.get(fieldName);
                builder.put(fieldName, deserialize(childNode));
            }
            return builder.build();
        } else if (node.isArray()) {
            RpcArray.Builder builder = new RpcArray.Builder();
            for (int i = 0; i < node.size(); i++) {
                TreeNode childNode = node.get(i);
                builder.add(deserialize(childNode));
            }
            return builder.build();
        } else {
            JsonNode n = ((JsonNode) node);
            if (n.isLong()) {
                return new RpcValue(new BigInteger(String.valueOf(n.asLong())));
            } else if (n.isInt()) {
                return new RpcValue(new BigInteger(String.valueOf(n.asInt())));
            } else if (n.isBoolean()) {
                return new RpcValue(n.asBoolean());
            }
            return new RpcValue(n.asText());
        }
    }

    private BigInteger convertDecimal(RpcValue value) {
        // The value of timestamp and nonce in v2 specs is a decimal string.
        // But there are decimal strings, numbers and 0x included hex strings in v2 blocks.
        // e.g.) "12345", 12345, "0x12345"
        //
        // RpcValue class converts numbers and 0x included hex strings to 0x included hex string
        // and holds it
        //
        // So, stringValue is a decimal string or a 0x included hex string.("12345", "0x12345")
        // if it has 0x, the method converts it as hex otherwise decimal

        String stringValue = value.asString();
        if (stringValue.startsWith(Bytes.HEX_PREFIX) ||
                stringValue.startsWith("-" + Bytes.HEX_PREFIX)) {
            return convertHex(value);
        } else {
            return new BigInteger(stringValue, 10);
        }
    }

    private BigInteger convertHex(RpcValue value) {
        // The value of 'value' and nonce in v2 specs is a decimal string.
        // But there are hex strings without 0x in v2 blocks.
        //
        // This method converts the value as hex no matter it has  0x prefix or not.

        String stringValue = value.asString();
        String sign = "";
        if (stringValue.charAt(0) == '-') {
            sign = stringValue.substring(0, 1);
            stringValue = stringValue.substring(1);
        }
        return new BigInteger(sign + Bytes.cleanHexPrefix(stringValue), 16);
    }
}
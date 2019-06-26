package foundation.icon.sample_iconex_connect;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

import foundation.icon.icx.transport.jsonrpc.Request;
import foundation.icon.icx.transport.jsonrpc.RpcArray;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;

public class RequestSerializer extends JsonSerializer<Request> {

    @Override
    public void serialize(
            foundation.icon.icx.transport.jsonrpc.Request item, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        serialize(item, gen);
    }

    private void serialize(foundation.icon.icx.transport.jsonrpc.Request item, JsonGenerator gen)
            throws IOException {

        gen.writeStartObject();
        gen.writeFieldName("id");
        gen.writeNumber(item.getId());
        gen.writeFieldName("jsonrpc");
        gen.writeString(item.getJsonrpc());
        gen.writeFieldName("method");
        gen.writeString(item.getMethod());
        gen.writeFieldName("params");
        serialize(item.getParams(), gen);
        gen.writeEndObject();
    }

    private void serialize(RpcItem item, JsonGenerator gen)
            throws IOException {

        if (item instanceof RpcObject) {
            RpcObject object = item.asObject();
            gen.writeStartObject();
            for (String key : object.keySet()) {
                RpcItem value = object.getItem(key);
                if (value != null) {
                    gen.writeFieldName(key);
                    serialize(value, gen);
                }
            }
            gen.writeEndObject();
        } else if (item instanceof RpcArray) {
            RpcArray array = item.asArray();
            gen.writeStartArray();
            for (RpcItem childItem : array) {
                serialize(childItem, gen);
            }
            gen.writeEndArray();
        } else {
            gen.writeString(item.asString());
        }
    }
}

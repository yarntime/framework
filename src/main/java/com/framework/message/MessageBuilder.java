package com.framework.message;

import java.lang.reflect.Type;
import java.util.Map;

import com.framework.response.BaseResponse;
import com.framework.response.ExceptionResponse;
import com.framework.utils.gson.APINoSee;
import com.framework.utils.gson.GsonTypeCoder;
import com.framework.utils.gson.GsonUtil;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MessageBuilder {
    private static MessageBuilder self;
    static {
        self = new MessageBuilder();
    }

    private MessageBuilder() {
        Encoder encoder = new Encoder();
        gsonEncoder =
                new GsonUtil().setCoder(Message.class, encoder)
                        .setExclusionStrategies(new ExclusionStrategy[] {encoder}).create();
        encoder.setGson(gsonEncoder);

        Decoder decoder = new Decoder();
        gsonDecoder = new GsonUtil().setCoder(Message.class, decoder).create();
        decoder.setGson(gsonDecoder);
    }

    public class MessageTypeAdapter implements JsonSerializer<Message>, JsonDeserializer<Message> {

        @Override
        public JsonElement serialize(Message message, Type type, JsonSerializationContext context) {
            return null;
        }

        @Override
        public Message deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {

            return null;
        }
    }

    private final Gson gsonEncoder;
    private final Gson gsonDecoder;

    private class Encoder implements GsonTypeCoder<Message>, ExclusionStrategy {
        private Gson gson;

        void setGson(Gson gson) {
            this.gson = gson;
        }

        @Override
        public Message deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObj = json.getAsJsonObject();
            Map.Entry<String, JsonElement> entry = jObj.entrySet().iterator().next();
            String className = entry.getKey();
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Unable to deserialize class " + className, e);
            }
            return (Message) this.gson.fromJson(entry.getValue(), clazz);
        }

        @Override
        public JsonElement serialize(Message msg, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObj = new JsonObject();
            jObj.add(msg.getClass().getName(), this.gson.toJsonTree(msg));
            return jObj;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(APINoSee.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }

    private class Decoder implements GsonTypeCoder<Message> {
        private Gson gson;

        void setGson(Gson gson) {
            this.gson = gson;
        }

        @Override
        public Message deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObj = json.getAsJsonObject();
            Map.Entry<String, JsonElement> entry = jObj.entrySet().iterator().next();
            String className = entry.getKey();
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Unable to deserialize class " + className, e);
            }
            Message msg = (Message) this.gson.fromJson(entry.getValue(), clazz);
            return msg;
        }

        @Override
        public JsonElement serialize(Message msg, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObj = new JsonObject();
            jObj.add(msg.getClass().getName(), this.gson.toJsonTree(msg));
            return jObj;
        }

    }

    public static Message loads(String jsonStr) {
        Message msg = self.gsonDecoder.fromJson(jsonStr, Message.class);
        return msg;
    }

    public static String dump(Message msg) {
        return self.gsonEncoder.toJson(msg, Message.class);
    }

    public static String dumpResponse(BaseResponse response, String action) {
        Gson gson = new Gson();
        JsonObject obj = new JsonObject();
        obj.add(action, gson.toJsonTree(response));
        return obj.toString();
    }

    public static String dumpResponse(BaseResponse response) {
        Gson gson = new Gson();
        return gson.toJson(response);
    }

    public static String dumpResponse(ExceptionResponse response) {
        Gson gson = new Gson();
        return gson.toJson(response);
    }
}

package co.giftree.maskeyes.util;

import com.google.gson.JsonElement;

import com.google.gson.JsonObject;

public class JsonUtil {
    public static String getNullAsEmptyString(JsonElement jsonElement) {
        if(jsonElement==null){
            return "";
        }else
            return jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
    }


    public static String hasJsonAndGetString(JsonObject jsonObject, String name){
        if(jsonObject.has(name)){
            return getNullAsEmptyString(jsonObject.get(name));
        }else{
            return "";
        }
    }


    public static String hasJsonAndGetNumberString(JsonObject jsonObject, String name){
        if(jsonObject.has(name)){
            return getNullAsEmptyNumberString(jsonObject.get(name));
        }else{
            return "0";
        }
    }

    public static String getNullAsEmptyNumberString(JsonElement jsonElement) {
        if(jsonElement==null){
            return "0";
        }else
            return jsonElement.isJsonNull() ? "0" : jsonElement.getAsString();
    }

    public static String hasJsonAndGetStringNullable(JsonObject jsonObject, String name){
        if(jsonObject.has(name)){
            return getNullAsEmptyString(jsonObject.get(name));
        }else{
            return null;
        }
    }
}
package Json;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class JsonConverter
{
    public static String toJson(Object object)
    {
        Gson gson = new Gson();
        String message = gson.toJson(object);
        return message;
    }

    public static Type fromJson(String text, Type type)
    {
        Gson gson = new Gson();
        return gson.fromJson(text, type);
    }
}

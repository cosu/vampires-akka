package ro.cosu.vampires.server.util.gson;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImmutableMapTypeAdapterFactory implements TypeAdapterFactory {

    public static <K, V> InstanceCreator<Map<K, V>> newCreator() {
        return type -> new HashMap<>();
    }

    @Nullable
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!ImmutableMap.class.isAssignableFrom(type.getRawType())) {
            return null;
        }
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            @SuppressWarnings("unchecked")
            public T read(JsonReader in) throws IOException {
                return (T) ImmutableMap.copyOf((Map) delegate.read(in));
            }
        };
    }
}

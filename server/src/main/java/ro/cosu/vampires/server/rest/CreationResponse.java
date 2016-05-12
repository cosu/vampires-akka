package ro.cosu.vampires.server.rest;


import com.google.auto.value.AutoValue;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class CreationResponse {
    public abstract String id();
}

package ro.cosu.vampires.server.actors.messages;

import com.google.auto.value.AutoValue;

import java.util.UUID;

import ro.cosu.vampires.server.resources.Resource;

@AutoValue
public abstract class BootstrapResource {
    public static BootstrapResource create(Resource.ProviderType providerType, String name) {
        return new AutoValue_BootstrapResource(providerType, name, UUID.randomUUID().toString());
    }

    public abstract Resource.ProviderType type();

    public abstract String name();

    public abstract String requestId();
}

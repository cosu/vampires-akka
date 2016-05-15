package ro.cosu.vampires.server.actors.messages;

import com.google.auto.value.AutoValue;

import java.util.UUID;

import ro.cosu.vampires.server.resources.Resource;

@AutoValue
public abstract class BootstrapResource {
    public static BootstrapResource create(Resource.ProviderType providerType, String name,
                                           String serverId) {
        return new AutoValue_BootstrapResource(providerType, name, serverId, UUID.randomUUID().toString());
    }

    public abstract Resource.ProviderType type();

    public abstract String name();

    public abstract String serverId();


    public abstract String requestId();
}

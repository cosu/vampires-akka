package ro.cosu.vampires.server.actors.messages;

import com.google.auto.value.AutoValue;

import ro.cosu.vampires.server.resources.Resource;

@AutoValue
public abstract class CreateResource {
    public static CreateResource create(Resource.ProviderType providerType, Resource.Parameters parameters) {
        return new AutoValue_CreateResource(providerType, parameters);
    }

    public abstract Resource.ProviderType type();

    public abstract Resource.Parameters parameters();

}

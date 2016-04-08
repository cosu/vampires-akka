package ro.cosu.vampires.client.extension;


import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.ExtensionIdProvider;

public class ExecutorsExtension extends AbstractExtensionId<ExecutorsExtensionImpl>
        implements ExtensionIdProvider {
    public final static ExecutorsExtension ExecutorsProvider = new ExecutorsExtension();

    private ExecutorsExtension() {
    }

    public ExecutorsExtension lookup() {
        return ExecutorsExtension.ExecutorsProvider;
    }

    public ExecutorsExtensionImpl createExtension(ExtendedActorSystem system) {
        return new ExecutorsExtensionImpl(system.settings().config());
    }

}

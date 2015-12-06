package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ro.cosu.vampires.server.resources.ResourceDescription;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.workload.ClientInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ResourceRegistry {
    protected List<ActorRef> resources = new LinkedList<>();
    protected BiMap<ActorRef, String> resourceActorsToClientIds = HashBiMap.create();
    protected BiMap<String, ResourceDescription> clientIdsToDescriptions = HashBiMap.create();
    protected BiMap<ClientInfo, ActorRef> registeredClients = HashBiMap.create();


    void addResource(ActorRef resource) {
        resources.add(resource);
    }

    void registerClient(ActorRef sender, ClientInfo register) {
        registeredClients.put(register, sender);

    }

    public BiMap<ClientInfo, ActorRef> getRegisteredClients() {
        return registeredClients;
    }

    ActorRef lookupResource(String clientId) {
        return resourceActorsToClientIds.inverse().get(clientId);
    }


    ActorRef lookupResource(ClientInfo clientInfo) {
        return resourceActorsToClientIds.inverse().get(clientInfo.id());
    }


    public List<ActorRef> getResources() {
        return resources;
    }

    public void registerResource(ActorRef sender, ResourceInfo resourceInfo) {
        resourceActorsToClientIds.put(sender, resourceInfo.description().id());
        clientIdsToDescriptions.put(resourceInfo.description().id(), resourceInfo.description());
    }

    public void removeResource(ActorRef sender) {
        final String clientId = resourceActorsToClientIds.get(sender);
        resourceActorsToClientIds.remove(sender);
        clientIdsToDescriptions.remove(clientId);

        final Optional<ClientInfo> first = registeredClients.keySet().stream().filter(clientInfo1 -> clientInfo1
                .id().equals(clientId)).findFirst();
        first.ifPresent(clientInfo -> registeredClients.remove(clientInfo));

    }
}

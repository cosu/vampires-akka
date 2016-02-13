package ro.cosu.vampires.server.actors;

import akka.actor.ActorRef;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ro.cosu.vampires.server.resources.ResourceInfo;
import ro.cosu.vampires.server.workload.ClientInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ResourceRegistry {
    /**
     * ResourceActors - local akka actors
     * clients - client remote akka actors
     * ClientIds - initialized at creation - shared by both remote and resource
     *
     */
    protected List<ActorRef> resourceActors = new LinkedList<>();
    protected BiMap<String, ActorRef> clientIdsToResourceActors = HashBiMap.create();
    protected BiMap<String, ActorRef> clientIdsToClientActors = HashBiMap.create();

    public void addResourceActor(ActorRef resource) {
        resourceActors.add(resource);
    }

    public BiMap<String, ActorRef> getRegisteredClients() {
        return clientIdsToClientActors;
    }

    public Optional<ActorRef> lookupResourceOfClient(String clientId) {
        return Optional.ofNullable(clientIdsToResourceActors.get(clientId));
    }

    public List<ActorRef> getResourceActors() {
        return resourceActors;
    }

    public void registerResource(ActorRef localResourceActor, ResourceInfo resourceInfo) {
        String clientId = resourceInfo.description().id();
        clientIdsToResourceActors.put(clientId, localResourceActor);
    }

    public void registerClient(ActorRef clientActor, ClientInfo clientInfo) {
        clientIdsToClientActors.put(clientInfo.id(), clientActor);
    }

    public void removeResource(ActorRef resourceActor) {
        String clientId = clientIdsToResourceActors.inverse().get(resourceActor);
        clientIdsToClientActors.remove(clientId);
        clientIdsToResourceActors.remove(clientId);
        resourceActors.remove(resourceActor);
    }
}

/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

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
     * ResourceActors - local akka actors clients - client remote akka actors ClientIds -
     * initialized at creation - shared by both remote and resource
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

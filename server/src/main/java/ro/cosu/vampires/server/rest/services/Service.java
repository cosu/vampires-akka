package ro.cosu.vampires.server.rest.services;


import java.util.Collection;
import java.util.Optional;

import ro.cosu.vampires.server.workload.Id;

public interface Service<T extends Id, P> {
    Collection<T> list();

    T create(P payload);

    Optional<T> delete(String id);

    Optional<T> update(T updated);

    Optional<T> get(String id);


}

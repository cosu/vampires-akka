package ro.cosu.vampires.server.rest.services;


import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;

public class ProvidersService {

    @Inject
    private List<String> providers;

    public Collection<String> list() {
        return providers;
    }

}

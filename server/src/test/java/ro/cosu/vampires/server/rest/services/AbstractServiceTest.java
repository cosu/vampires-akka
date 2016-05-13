package ro.cosu.vampires.server.rest.services;


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import ro.cosu.vampires.server.workload.Id;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public abstract class AbstractServiceTest<T extends Id, P> {

    private Service<T, P> instance;

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(getModule());
        instance = injector.getInstance(Key.get(getTypeTokenService()));
        instance.create(getPayload());
    }

    protected abstract AbstractModule getModule();


    protected abstract TypeLiteral<Service<T, P>> getTypeTokenService();

    protected abstract P getPayload();

    protected T create() {
        return instance.create(getPayload());
    }

    @Test
    public void list() throws Exception {
        assertThat(instance.list().size(), is(1));
    }

    @Test
    public void delete() throws Exception {
        assertThat(instance.list().size(), is(1));
        String id = instance.list().iterator().next().id();
        Optional<T> delete = instance.delete(id);
        assertThat(delete.isPresent(), is(true));
        assertThat(instance.list().size(), is(0));
    }


    @Test
    public void update() throws Exception {
        assertThat(instance.list().size(), is(1));
        T next = instance.list().iterator().next();
        Optional<T> update = instance.update(next);
        assertThat(update.isPresent(), is(true));
    }

    @Test
    public void get() throws Exception {
        assertThat(instance.list().size(), is(1));
        T next = instance.list().iterator().next();
        Optional<T> optional = instance.get(next.id());
        assertThat(optional.isPresent(), is(true));
    }

}

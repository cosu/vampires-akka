/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

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

    protected Service<T, P> instance;

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


    public abstract void update() throws Exception;

    @Test
    public void get() throws Exception {
        assertThat(instance.list().size(), is(1));
        T next = instance.list().iterator().next();
        Optional<T> optional = instance.get(next.id());
        assertThat(optional.isPresent(), is(true));
    }

}

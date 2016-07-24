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

package ro.cosu.vampires.client.executors.fork;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.server.values.jobs.Computation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class ForkExecutorTest {


    private Executor getFork() {
        Injector injector = Guice.createInjector(new ForkExecutorModule(ConfigFactory.load().getConfig("vampires")));

        return injector.getInstance(Executor.class);

    }

    @Test
    public void testNoCommand() throws Exception {
        Computation computation = Computation.builder().id("test").command("bla").build();

        Executor executor = getFork();

        assertThat(executor.execute(computation).exitCode(), not(0));
    }

    @Test
    public void testExecuteFail() throws Exception {

        Computation computation = Computation.builder().id("test").command("cat /dev/null1").build();

        Executor executor = getFork();

        assertThat(executor.execute(computation).exitCode(), is(not(0)));
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        Computation computation = Computation.builder().id("test").command("cat /dev/null").build();

        Executor executor = getFork();

        assertThat(executor.execute(computation).exitCode(), is(0));
    }

    @Test
    public void testGetNCPU() throws Exception {

        Executor executor = getFork();
        assertThat(executor.getNCpu(), not(0));

    }
}

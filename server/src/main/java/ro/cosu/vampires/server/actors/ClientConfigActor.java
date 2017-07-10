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

package ro.cosu.vampires.server.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.values.ClientConfig;
import ro.cosu.vampires.server.values.ClientInfo;

public class ClientConfigActor extends AbstractActor {
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props() {
        return Props.create(ClientConfigActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ClientInfo.class, clientInfo-> {
            final ClientConfig configFor = getConfigFor(clientInfo);
            log.debug("config for client {} {} {}", clientInfo.id(),
                    clientInfo.metrics().metadata().get("host-hostname"), configFor);
            getSender().tell(configFor, getSelf());
        }).build();
    }

    private ClientConfig getConfigFor(ClientInfo clientInfo) {
        //take the first executor defined in the config
        return settings
                .getExecutors().stream()
                .filter(ex -> clientInfo.executors().containsKey(ex))
                .findFirst()
                .map(executor -> getConfig(executor, clientInfo))
                .orElseGet(() -> {
                    log.error("client has reported unsupported executors: {} ", clientInfo.executors());
                    return ClientConfig.empty();
                });

    }

    private ClientConfig getConfig(String executor, ClientInfo clientInfo) {
        int executorCpuCount = clientInfo.executors().get(executor);
        int cpuSetSize = Math.min(executorCpuCount, settings.getCpuSetSize());
        //this happens if the config says use 2 cpus but the machine has only 1.
        if (executorCpuCount < settings.getCpuSetSize()) {
            log.warning("Client reported less cpus ({}) than CPU_SET_SIZE ({})", executorCpuCount,
                    settings.getCpuSetSize());
        }
        int numberOfExecutors = executorCpuCount / cpuSetSize;

        return ClientConfig.builder()
                .cpuSetSize(cpuSetSize)
                .executor(executor)
                .numberOfExecutors(numberOfExecutors)
                .build();
    }


}

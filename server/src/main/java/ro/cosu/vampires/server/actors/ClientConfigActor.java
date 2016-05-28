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

import java.util.Optional;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ro.cosu.vampires.server.actors.settings.Settings;
import ro.cosu.vampires.server.actors.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.ClientConfig;
import ro.cosu.vampires.server.workload.ClientInfo;

public class ClientConfigActor extends UntypedActor {
    private final SettingsImpl settings =
            Settings.SettingsProvider.get(getContext().system());
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props() {
        return Props.create(ClientConfigActor.class);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ClientInfo) {
            ClientInfo clientInfo = (ClientInfo) message;
            final ClientConfig configFor = getConfigFor(clientInfo);
            log.info("config for client {}:{} {}", clientInfo.metrics().metadata().get("host-hostname"),
                    clientInfo.id(), configFor);
            getSender().tell(configFor, getSelf());
        } else {
            unhandled(message);
        }
    }

    private ClientConfig getConfigFor(ClientInfo clientInfo) {
        //take the first executor defined in the config
        final Optional<String> firstAvailableExecutor = settings
                .getExecutors().stream()
                .filter(ex -> clientInfo.executors().containsKey(ex))
                .findFirst();


        if (!firstAvailableExecutor.isPresent()) {
            log.error("client has reported unsupported executors: {} ", clientInfo.executors());
            return ClientConfig.empty();
        }

        final String executor = firstAvailableExecutor.get();
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

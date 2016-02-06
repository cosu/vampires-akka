package ro.cosu.vampires.client.executors.fork;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.apache.commons.exec.DefaultExecutor;
import ro.cosu.vampires.client.executors.Executor;
import ro.cosu.vampires.client.monitoring.HostInfo;


public class ForkModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(Executor.class).to(ForkExecutor.class);
    }

    @Provides
    private  org.apache.commons.exec.Executor provideExecutor(){
        return  new DefaultExecutor();
    }



    @Provides
    @Named("cpuCount")
    private  int provideCpuCount(){
        return HostInfo.getAvailableProcs();
    }
}

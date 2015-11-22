package ro.cosu.vampires.client.executors.fork;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.commons.exec.DefaultExecutor;
import ro.cosu.vampires.client.executors.Executor;


public class ForkModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(Executor.class).to(ForkExecutor.class);
    }

    @Provides
    org.apache.commons.exec.Executor provideExecutor(){
        return  new DefaultExecutor();
    }


}

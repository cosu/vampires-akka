package ro.cosu.vampires.client.executors;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

public class CommandModule extends AbstractModule{
    @Override
    protected void configure() {
        MapBinder<Executor.Type, Executor> mapbinder
                = MapBinder.newMapBinder(binder(), Executor.Type.class, Executor.class);

        mapbinder.addBinding(Executor.Type.COMMAND).to(CommandExecutor.class);
    }
}

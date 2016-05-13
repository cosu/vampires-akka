package ro.cosu.vampires.server.rest.services;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.ExecutionPayload;


public class ExecutionsServiceTest extends AbstractServiceTest<Execution, ExecutionPayload> {

    private static ActorSystem system;

    public static ActorSystem getActorSystem() {
        return system;
    }

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("test", ConfigFactory.load("application-dev.conf"));
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Override
    protected AbstractModule getModule() {
        return new ServicesTestModule(system);
    }

    @Override
    protected TypeLiteral<Service<Execution, ExecutionPayload>> getTypeTokenService() {
        return ExecutionsService.getTypeTokenService();
    }

    @Override
    protected ExecutionPayload getPayload() {
        return ExecutionPayload.builder().configuration("foo").workload("bar").type(ExecutionMode.FULL).build();
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void delete() throws Exception {
        super.delete();
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void update() throws Exception {
        super.update();
    }
}
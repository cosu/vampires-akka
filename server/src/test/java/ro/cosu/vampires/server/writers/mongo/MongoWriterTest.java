package ro.cosu.vampires.server.writers.mongo;

import com.google.common.collect.Maps;

import com.mongodb.MongoClient;

import org.junit.Test;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.metrics.Metrics;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MongoWriterTest {
    @Test
    public void addResult() throws Exception {
        Morphia morphia = mock(Morphia.class);
        Datastore datastore = mock(Datastore.class);
        MongoClient mongoClient = mock(MongoClient.class);
        when(morphia.createDatastore(Mockito.eq(mongoClient), anyString())).thenReturn(datastore);
        MongoWriter mongoWriter = new MongoWriter(morphia, mongoClient);
        Job empty = Job.empty();
        mongoWriter.addResult(empty);
        ClientInfo clientInfo = ClientInfo.builder()
                .id("foo")
                .executors(Maps.newHashMap())
                .metrics(Metrics.empty())
                .build();
        mongoWriter.addClient(clientInfo);

        verify(datastore).save(clientInfo);
        verify(datastore).save(empty);

    }


}
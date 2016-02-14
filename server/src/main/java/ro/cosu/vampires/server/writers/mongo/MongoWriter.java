package ro.cosu.vampires.server.writers.mongo;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import ro.cosu.vampires.server.workload.ClientInfo;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.writers.ResultsWriter;

public class MongoWriter implements ResultsWriter {
    private Morphia morphia = new Morphia();
    private final Datastore datastore;

    public MongoWriter(){
        morphia.mapPackage("ro.cosu.vampires.server.writers.mongo");
        morphia.getMapper().getConverters().addConverter(LocalDateTimeConverter.class);

        datastore = morphia.createDatastore(getMongoClient(), "vampires");
        datastore.ensureIndexes();

    }

    private MongoClient getMongoClient() {
        return new MongoClient();
    }

    @Override
    public void addResult(Job result) {
        datastore.save(result);
    }

    @Override
    public void addClient(ClientInfo clientInfo) {
     datastore.save(clientInfo);
    }

    @Override
    public void close() {
        // nothing
    }
}

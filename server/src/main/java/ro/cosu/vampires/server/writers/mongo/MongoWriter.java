package ro.cosu.vampires.server.writers.mongo;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.writers.ResultsWriter;

public class MongoWriter implements ResultsWriter {
    final Morphia morphia = new Morphia();
    final Datastore datastore;

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
    public void writeResult(Workload result) {
        datastore.save(result);
    }

    @Override
    public void close() {
        // nothing
    }
}

package ro.cosu.vampires.server.writers.mongo;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.writers.ResultsWriter;

public class MongoWriter implements ResultsWriter {
    final Morphia morphia = new Morphia();


    MongoWriter(){
        morphia.mapPackage("ro.cosu.vampires.server.workload");
        final Datastore datastore = morphia.createDatastore(getMongoClient(), "vampires");
        datastore.ensureIndexes();

    }


    private MongoClient getMongoClient() {
        return new MongoClient();
    }

    @Override
    public void writeResult(Workload result) {

    }

    @Override
    public void close() {
        // nothing
    }
}

package ro.cosu.vampires.server.writers.mongo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import ro.cosu.vampires.server.workload.Workload;

@Entity(noClassnameStored=true)
public class MongoResult {

    @Id
    private ObjectId id;

    private Workload workload;

    public Workload getWorkload() {
        return workload;
    }

    public void setWorkload(Workload workload) {
        this.workload = workload;
    }



    public MongoResult() {}
}

package ro.cosu.vampires.server.writers.mongo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import ro.cosu.vampires.server.workload.Job;

@Entity(noClassnameStored=true)
public class MongoResult {

    @Id
    private ObjectId id;

    private Job job;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }



    public MongoResult() {}
}

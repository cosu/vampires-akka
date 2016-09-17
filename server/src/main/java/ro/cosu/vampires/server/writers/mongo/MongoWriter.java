/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.writers.mongo;

import com.mongodb.MongoClient;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import ro.cosu.vampires.server.values.ClientInfo;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.writers.ResultsWriter;

public class MongoWriter implements ResultsWriter {
    private final Datastore datastore;

    public MongoWriter(Morphia morphia, MongoClient mongoClient) {
        datastore = morphia.createDatastore(mongoClient, "vampires");
        datastore.ensureIndexes();
    }

    @SuppressWarnings("unused")
    protected static Morphia getMorphia() {
        Morphia morphia = new Morphia();
        morphia.mapPackage("ro.cosu.vampires.server.writers.mongo");
        morphia.getMapper().getConverters().addConverter(LocalDateTimeConverter.class);
        return morphia;
    }

    public static MongoWriter newDefaultWriter() {
        return new MongoWriter(getMorphia(), getMongoClient());
    }

    protected static MongoClient getMongoClient() {
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

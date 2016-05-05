package ro.cosu.vampires.server.rest.controllers;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ro.cosu.vampires.server.actors.AbstractActorTest;
import spark.Spark;
import spark.utils.IOUtils;


public class AbstractControllerTest extends AbstractActorTest {
    protected static Injector injector;


    @BeforeClass
    public static void setup() {
        AbstractActorTest.setup();
        Spark.init();
        Spark.awaitInitialization();

        ControllersModule controllersModule = new ControllersModule(system);
        injector = Guice.createInjector(controllersModule);
    }

    @AfterClass
    public static void teardown() {
        injector = null;
        Spark.stop();
        AbstractActorTest.teardown();
    }

    protected Response request(String method, String path, String payload) {
        try {
            URL url = new URL("http://localhost:4567" + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            if (payload.length() > 0) {
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                OutputStream os = connection.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.close();

            }
            connection.connect();
            String body = IOUtils.toString(connection.getInputStream());

            return new Response(connection.getResponseCode(), body);
        } catch (IOException e) {
            return new Response(404, "");
        }
    }

    protected static class Response {
        public final int status;
        public final String body;

        private Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}

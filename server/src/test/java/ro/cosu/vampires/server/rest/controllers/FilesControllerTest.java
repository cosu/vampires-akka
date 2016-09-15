package ro.cosu.vampires.server.rest.controllers;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.values.FileInfo;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static ro.cosu.vampires.server.rest.controllers.AbstractControllerTest.getHttpClient;
import static ro.cosu.vampires.server.rest.controllers.AbstractControllerTest.url;

public class FilesControllerTest {

    private static FilesController filesController;
    private Gson gson = JsonTransformer.get().getGson();

    @BeforeClass
    public static void setUpClass() {
        Spark.init();
        Config config = ConfigFactory.parseMap(ImmutableMap.of("uploadDir", "/tmp"));
        filesController = new FilesController(config);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Spark.stop();
    }

    @Test
    public void get() throws Exception {
        HttpClient client = getHttpClient();
        HttpGet httpGet = new HttpGet(url + "/upload");
        HttpResponse execute = client.execute(httpGet);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_OK));
        FileInfo[] fileInfos = gson.fromJson(EntityUtils.toString(execute.getEntity()), FileInfo[].class);
        assertThat(fileInfos.length, is(0));
    }


}
package ro.cosu.vampires.server.rest.controllers;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import ro.cosu.vampires.server.rest.JsonTransformer;
import ro.cosu.vampires.server.values.FileInfo;
import spark.Spark;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static ro.cosu.vampires.server.rest.controllers.AbstractControllerTest.getHttpClient;
import static ro.cosu.vampires.server.rest.controllers.AbstractControllerTest.url;

public class FilesControllerTest {

    private static FilesController filesController;
    private static String BODY = "test";
    private Gson gson = JsonTransformer.get().getGson();

    @BeforeClass
    public static void setUpClass() {
        Spark.init();
        Config config = ConfigFactory.parseMap(ImmutableMap.of("uploadDir", "/tmp"));
        filesController = new FilesController(config);
    }

    @Before
    public void setUp() throws Exception {
        Spark.awaitInitialization();
    }


    @Test
    public void list() throws Exception {
        upload();
        FileInfo[] fileInfos = getFileInfos();
        assertThat(fileInfos.length, is(1));
        assertThat(fileInfos[0].name(), is("test.txt"));
    }

    private FileInfo[] getFileInfos() throws IOException {
        HttpClient client = getHttpClient();
        HttpGet httpGet = new HttpGet(url + "/upload");
        HttpResponse execute = client.execute(httpGet);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_OK));
        return gson.fromJson(EntityUtils.toString(execute.getEntity()), FileInfo[].class);
    }

    @Test
    public void upload() throws Exception {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        builder.addBinaryBody("file", BODY.getBytes(Charsets.UTF_8), ContentType.APPLICATION_OCTET_STREAM, "test.txt");
        HttpEntity entity = builder.build();
        HttpPost post = new HttpPost(url + "/upload");
        post.setEntity(entity);
        HttpResponse execute = getHttpClient().execute(post);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_CREATED));
    }

    @Test
    public void delete() throws Exception {
        upload();
        FileInfo fileInfo = getFileInfos()[0];

        HttpDelete httpDelete = new HttpDelete(url + "/upload/" + fileInfo.id());
        HttpResponse execute = getHttpClient().execute(httpDelete);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_NO_CONTENT));
    }

    @Test
    public void get() throws Exception {
        upload();
        FileInfo fileInfo = getFileInfos()[0];

        HttpGet httpGet = new HttpGet(url + "/upload/" + fileInfo.id());

        HttpResponse execute = getHttpClient().execute(httpGet);
        assertThat(execute.getStatusLine().getStatusCode(), is(HTTP_OK));
        String body = EntityUtils.toString(execute.getEntity());
        assertThat(body, is(BODY));

    }

}
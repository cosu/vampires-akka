package ro.cosu.vampires.server.rest.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import spark.utils.IOUtils;


public class Response {
    public final int status;
    public final String body;
    public Response(int status, String body) {
        this.status = status;
        this.body = body;
    }

    /*
    This is a dumb utility class to fire up http requests. Use only for small tests
     */
    public static Response request(String method, String path, String payload) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://localhost:4567" + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            if (payload.length() > 0) {
                connection.setRequestProperty("Content-ProviderType", "application/json; charset=UTF-8");
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
            return new Response(connection.getResponseCode(), "");
        }
    }
}

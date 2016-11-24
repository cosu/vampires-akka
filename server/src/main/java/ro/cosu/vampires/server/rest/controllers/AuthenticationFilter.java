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

package ro.cosu.vampires.server.rest.controllers;

import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Set;

import spark.Request;
import spark.Response;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static spark.Spark.before;
import static spark.Spark.halt;

public class AuthenticationFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    private static Set<String> storedCredentials = Sets.newHashSet();

    {
        // lame static store for now
        storedCredentials.add("admin:admin");
        storedCredentials.add("user:user");
    }

    public AuthenticationFilter() {
        // stupid authentication
        before((request, response) -> {
            boolean authenticated = !request.session().isNew();
            String auth = request.headers("Authorization");
            if (request.requestMethod().equals("OPTIONS"))
                authenticated = true;

            if (!authenticated) {
                // try to authenticate
                if (auth != null && auth.startsWith("Basic")) {
                    String credentials = getCredentialsFromHeader(auth);
                    if (storedCredentials.contains(credentials)) {
                        authenticated = true;
                        // store the user in the session
                        request.session().attribute("user", credentials.split(":")[0]);
                        // 10 min sessions
                        request.session().maxInactiveInterval(600);
                    }
                } else {
                    LOG.error("Missing or Incomplete Authorization Header");
                    unauthorized(request, response);
                }
            }
            // check result
            if (!authenticated) {
                unauthorized(request, response);
            }
        });
        LOG.info("Authentication filter enabled");
    }

    private static String getCredentialsFromHeader(String auth) {
        String b64Credentials = auth.substring("Basic".length()).trim();
        return new String(Base64.getDecoder().decode(b64Credentials), UTF_8);
    }

    private void unauthorized(Request request, Response response) {
        LOG.warn("unauthorized client {} url {} headers {}", request.ip(), request.url(), request.headers("Authorization"));
        response.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
        halt(HTTP_UNAUTHORIZED);
    }
}

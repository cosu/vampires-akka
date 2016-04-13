/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.resources.das5;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResourceProvider;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.util.SshClient;

import java.util.Optional;

public class Das5ResourceProvider extends AbstractResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(Das5ResourceProvider.class);

    @Inject
    @Named("DASSSH")
    private SshClient sshClient;

    @Override
    public Optional<Resource> create(Resource.Parameters parameters) {

        if (parameters instanceof Das5ResourceParameters)
            return Optional.of(new Das5Resource((Das5ResourceParameters) parameters, sshClient));
        else {
            LOG.error("invalid parameter type. expected " + Das5ResourceParameters.class);
            return Optional.empty();
        }
    }

    @Override
    public Resource.Type getType() {
        return Resource.Type.DAS5;
    }

    @Override
    public Resource.Parameters.Builder getBuilder() {
        return Das5ResourceParameters.builder();
    }
}

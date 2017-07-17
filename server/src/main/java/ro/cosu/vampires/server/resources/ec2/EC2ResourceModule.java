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

package ro.cosu.vampires.server.resources.ec2;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nullable;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.resources.ResourceProvider;

public class EC2ResourceModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(EC2ResourceModule.class);

    protected static AmazonEC2 getAmazonEC2Client(String credentialsFile) {
        AmazonEC2 amazonEC2Client = null;

        LOG.debug("reading credentials  AWS from {}", credentialsFile);
        try {
            PropertiesCredentials credentials = new PropertiesCredentials(new FileInputStream(credentialsFile));
            amazonEC2Client = AmazonEC2ClientBuilder.standard()
                    .withRegion(Regions.DEFAULT_REGION)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        } catch (IllegalArgumentException e) {
            LOG.error("Invalid EC2 file format", e);
        } catch (FileNotFoundException e) {
            LOG.error("could not find ec2 credentials file: " + credentialsFile);
        } catch (IOException e) {
            LOG.error("failed to create amazon client", e);
        }

        return amazonEC2Client;
    }

    @Override
    protected void configure() {
        MapBinder<Resource.ProviderType, ResourceProvider> mapbinder
                = MapBinder.newMapBinder(binder(), Resource.ProviderType.class, ResourceProvider.class);
        mapbinder.addBinding(Resource.ProviderType.EC2).to(EC2ResourceProvider.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Nullable
    private AmazonEC2 provideAmazonEc2(@Named("Config") Config config) {
        AmazonEC2 amazonEC2Client = null;
        if (config.hasPath("resources.ec2.credentialsFile")) {
            String credentialsFile = config.getString("resources.ec2.credentialsFile");
            amazonEC2Client = getAmazonEC2Client(credentialsFile);
        }
        return amazonEC2Client;
    }
}

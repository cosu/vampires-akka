package ro.cosu.vampires.server.resources;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created on 6-2-16.
 */
public class ResourceDescriptionTest {
    @Test
    public void testCreateResourceDescription() throws Exception {
        ResourceDescription test = ResourceDescription.create("test", Resource.Type.LOCAL);
        assertThat(test.id(), is(equalTo("test")));
        assertThat(test.provider(), is(equalTo(Resource.Type.LOCAL)));
    }
}
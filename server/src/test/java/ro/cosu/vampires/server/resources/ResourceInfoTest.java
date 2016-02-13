package ro.cosu.vampires.server.resources;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class ResourceInfoTest {
    @Test
    public  void testUnknown(){
        ResourceInfo unknown = ResourceInfo.unknown(Resource.Type.MOCK);
        assertThat(unknown.status(), is(Resource.Status.UNKNOWN));
    }

}
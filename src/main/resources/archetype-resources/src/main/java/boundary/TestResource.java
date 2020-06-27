package ${package}.boundary;

import ${package}.services.TestService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/test")
public class TestResource {

    @Inject
    private TestService testService;

    @GET
    public void test() {
        testService.test();
    }
}

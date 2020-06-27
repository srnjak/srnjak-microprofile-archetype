package ${package}.services;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@LocalBean
@Stateless
public class TestService {

    public void test() {
        System.out.println("TEST");
    }
}

package vn.iotstar;
import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest; import static org.junit.jupiter.api.Assertions.assertNotNull;
@SpringBootTest(properties={"spring.config.import=optional:classpath:/application-test.properties"}) class CustomLoginSecurityTest { @Autowired CustomLoginApplication application; @Test void applicationContextLoads(){assertNotNull(application);} }

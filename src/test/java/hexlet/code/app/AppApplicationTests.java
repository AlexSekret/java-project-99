package hexlet.code.app;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "sentry.enabled=false",
        "sentry.dsn="
})
class AppApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true);
    }

}

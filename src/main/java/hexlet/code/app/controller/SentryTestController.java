package hexlet.code.app.controller;

import io.sentry.Sentry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentryTestController {
    @GetMapping(path = "/error-test")
    public String testSentry() {
        try {
            throw new RuntimeException("This is a test exception for Sentry");
        } catch (Exception e) {
            Sentry.captureException(e);
            return "Test exception sent to Sentry!";
        }
    }
}

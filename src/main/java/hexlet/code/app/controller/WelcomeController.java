package hexlet.code.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    @GetMapping(path = "/welcome", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    public String welcome() {
        return "Welcome to Spring!";
    }

    @GetMapping(path = "/error-test")
    public String testSentryManual() throws Exception {
        throw new Exception("Ручная отправка в Sentry");
    }
}

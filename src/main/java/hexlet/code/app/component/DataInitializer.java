package hexlet.code.app.component;

import hexlet.code.app.model.User;
import hexlet.code.app.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String email = "hexlet@example.com";
        if (!userDetailsService.userExists(email)) {
            String password = "qwerty";
            User userData = new User();
            userData.setEmail(email);
            userData.setPasswordDigest(password);
            userDetailsService.createUser(userData);
        }
    }
}

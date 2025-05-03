package hexlet.code.app.component;

import hexlet.code.app.model.User;
import hexlet.code.app.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.stereotype.Component;

//@Profile("dev")
@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String firstName = "Admin";
        String lastName = "Admin";
        String email = "hexlet@example.com";
        String password = "qwerty";
        User userData = new User();
        userData.setEmail(email);
        userData.setPasswordDigest(password);
        userDetailsService.createUser(userData);
    }
}

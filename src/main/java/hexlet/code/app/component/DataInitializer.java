package hexlet.code.app.component;

import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@Profile("dev")
@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TaskStatusRepository taskRepository;

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

        ArrayList<String> defaultStatuses = new ArrayList<>(List.of("draft", "to_review",
                "to_be_fixed", "to_publish", "published"));
        List<String> currentStatuses = taskRepository.findAll().stream()
                .map(TaskStatus::getSlug)
                .toList();
        defaultStatuses.forEach(status -> {
            if (!currentStatuses.contains(status)) {
                TaskStatus taskStatus = new TaskStatus();
                taskStatus.setName(status);
                taskStatus.setSlug(status);
                taskRepository.save(taskStatus);
            }
        });
    }
}

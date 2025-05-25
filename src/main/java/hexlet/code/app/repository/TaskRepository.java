package hexlet.code.app.repository;

import hexlet.code.app.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>,
        JpaSpecificationExecutor<Task>, PagingAndSortingRepository<Task, Long> {
    Optional<Task> findByName(String name);
}

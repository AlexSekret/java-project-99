package hexlet.code.repository;

import hexlet.code.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long>, PagingAndSortingRepository<Label, Long> {
    Optional<Label> findByName(String name);
}

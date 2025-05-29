package hexlet.code.specification;

import hexlet.code.dto.user.UserParamsDTO;
import hexlet.code.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserSpecification {
    public Specification<User> build(UserParamsDTO params) {
        return withUserId(params.getIdCont())
                .and(withUserEmail(params.getEmailCont()));
    }

    private Specification<User> withUserId(Long userId) {
        return (root, query, cb) -> userId == null ? cb.conjunction() : cb.equal(root.get("id"), userId);
    }

    private Specification<User> withUserEmail(String userEmail) {
        return (root, query, cb) -> userEmail == null ? cb.conjunction() : cb.equal(root.get("email"), userEmail);
    }

    private Specification<User> withUserFirstName(String name) {
        return (root, query, cb) -> name == null ? cb.conjunction() : cb.equal(root.get("firstName"), name);
    }

    private Specification<User> withUserLastName(String name) {
        return (root, query, cb) -> name == null ? cb.conjunction() : cb.equal(root.get("lastName"), name);
    }

    private Specification<User> withCreatedAtGt(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.greaterThan(root.get("createdAt"), date);
    }

    private Specification<User> withCreatedAtLt(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.lessThan(root.get("createdAt"), date);
    }
}

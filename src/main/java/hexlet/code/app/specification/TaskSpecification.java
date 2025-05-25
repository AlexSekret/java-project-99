package hexlet.code.app.specification;

import hexlet.code.app.dto.TaskParamsDTO;
import hexlet.code.app.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {

    public Specification<Task> build(TaskParamsDTO params) {
        String title = params.getTitleCont();
        String status = params.getStatus();
        Long labelId = params.getLabelId();
        Long assigneeId = params.getAssigneeId();

        return withTaskTitle(title)
                .and(withAssigneeId(assigneeId))
                .and(withStatus(status))
                .and(withLabelId(labelId));
    }

    private Specification<Task> withLabelId(Long id) {
        return (root, query, cb) -> id == null ? cb.conjunction()
                : cb.equal(root.join("labels").get("id"), id);
    }

    private Specification<Task> withStatus(String status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("taskStatus").get("slug"), status);
    }

    private Specification<Task> withAssigneeId(Long id) {
        return (root, query, cb) -> id == null ? cb.conjunction()
                : cb.equal(root.get("assignee").get("id"), id);
    }

    private Specification<Task> withTaskTitle(String title) {
        return (root, query, cb) -> title == null ? cb.conjunction()
                : cb.like(root.get("name"), "%" + title + "%");
    }
}

package hexlet.code.specification;

import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {

    public Specification<Task> build(TaskParamsDTO params) {
        String title = params.getTitleCont();
        String status = params.getStatus();
        Integer labelId = params.getLabelId();
        Integer assigneeId = params.getAssigneeId();

        return withTaskTitle(title)
                .and(withAssigneeId(assigneeId))
                .and(withStatus(status))
                .and(withLabelId(labelId));
    }

    private Specification<Task> withLabelId(Integer id) {
        return (root, query, cb) -> id == null ? cb.conjunction()
                : cb.equal(root.join("labels").get("id"), id);
    }

    private Specification<Task> withStatus(String status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("taskStatus").get("slug"), status);
    }

    private Specification<Task> withAssigneeId(Integer id) {
        return (root, query, cb) -> id == null ? cb.conjunction()
                : cb.equal(root.get("assignee").get("id"), id);
    }

    private Specification<Task> withTaskTitle(String title) {
        return (root, query, cb) -> title == null ? cb.conjunction()
                : cb.like(root.get("name"), "%" + title + "%");
    }
}

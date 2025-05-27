package hexlet.code.app.dto.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskParamsDTO {
    private String titleCont;
    private Integer assigneeId;
    private String status;
    private Integer labelId;
}

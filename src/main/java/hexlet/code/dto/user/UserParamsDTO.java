package hexlet.code.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserParamsDTO {
    private Long idCont;
    private String emailCont;
    private String firstNameCont;
    private String lastNameCont;
    private LocalDate createdAtGt;
    private LocalDate createdAtLt;
}

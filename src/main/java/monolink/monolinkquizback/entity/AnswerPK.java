package monolink.monolinkquizback.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerPK implements Serializable {

    private Integer idQuestion;
    private String idAnswer;

}

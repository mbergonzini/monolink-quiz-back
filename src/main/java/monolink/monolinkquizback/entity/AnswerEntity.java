package monolink.monolinkquizback.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answer")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerEntity {

    @EmbeddedId
    private AnswerPK id;
    private String text;
    private Boolean isGoodAnswer;

}

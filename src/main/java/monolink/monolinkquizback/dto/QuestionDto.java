package monolink.monolinkquizback.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionDto {

    private Integer id;

    private List<AnswerDto> answers;
}

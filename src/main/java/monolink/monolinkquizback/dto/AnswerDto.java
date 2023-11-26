package monolink.monolinkquizback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerDto {

    private String id;
    private String text;
    private Boolean isGoodAnswer;
}

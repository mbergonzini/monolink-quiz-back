package monolink.monolinkquizback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseDto {
    private Integer questionId;
    private String answerId;
    private Double time;
}

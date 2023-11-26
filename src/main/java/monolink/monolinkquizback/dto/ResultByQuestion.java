package monolink.monolinkquizback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultByQuestion {

    private Integer questionId;
    private Double percentage;
    private Double time;
    private String popularResponse;

}

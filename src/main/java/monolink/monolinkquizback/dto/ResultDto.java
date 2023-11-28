package monolink.monolinkquizback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultDto {

    private Double percentage;
    private Double time;
    private String note;
}

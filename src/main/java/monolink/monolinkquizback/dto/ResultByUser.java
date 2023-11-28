package monolink.monolinkquizback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultByUser {

    private String mail;
    private Double percentage;
    private Double time;

}

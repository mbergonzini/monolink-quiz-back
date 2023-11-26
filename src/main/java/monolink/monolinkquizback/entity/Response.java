package monolink.monolinkquizback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private Integer questionId;
    private String answerId;
    private Double time;

}

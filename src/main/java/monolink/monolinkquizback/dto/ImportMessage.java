package monolink.monolinkquizback.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImportMessage extends ApiMessage {
    private int count;

    public ImportMessage(String message, int count) {
        super(message);
        this.count = count;
    }
}

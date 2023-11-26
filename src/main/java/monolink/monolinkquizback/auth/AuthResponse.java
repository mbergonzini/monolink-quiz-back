package monolink.monolinkquizback.auth;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private UUID id;
    private String userName;
    private String email;
    private List<String> roles;

}

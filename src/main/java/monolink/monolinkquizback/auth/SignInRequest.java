package monolink.monolinkquizback.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SignInRequest {
    @NotNull
    private String userName;

    @NotNull
    private String password;
}

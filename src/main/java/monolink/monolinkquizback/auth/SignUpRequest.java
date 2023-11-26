package monolink.monolinkquizback.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SignUpRequest {

    @NotBlank(message = "Le pseudo ne peut pas être vide")
    @Pattern(regexp = "^\\w{3,20}$", message = "Le pseudo doit contenir entre 3 et 20 caractères alphanumériques")
    private String userName;

    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(message = "L'email doit être valide")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@insee.fr$", message = "Cet email n'a pas le bon domaine")
    @Length(max = 50, message = "L'email ne peut pas contenir plus de 50 caractères")
    private String email;

    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[#$%+=!'])(?=\\S+$).{8,}$", message = "Le mot de passe doit " +
            "contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre , un caractère spécial (#$%+=!') et sans espace")
    private String password;

}

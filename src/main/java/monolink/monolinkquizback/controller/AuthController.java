package monolink.monolinkquizback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import monolink.monolinkquizback.auth.AuthResponse;
import monolink.monolinkquizback.auth.SignInRequest;
import monolink.monolinkquizback.auth.SignUpRequest;
import monolink.monolinkquizback.auth.UserDetailsImpl;
import monolink.monolinkquizback.controller.exceptions.ApiAuthSpecificValidationException;
import monolink.monolinkquizback.controller.exceptions.dto.ApiFieldError;
import monolink.monolinkquizback.dto.ApiMessage;
import monolink.monolinkquizback.repository.RoleRepository;
import monolink.monolinkquizback.repository.UserRepository;
import monolink.monolinkquizback.service.JwtService;
import monolink.monolinkquizback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Auth", description = "Auth management APIs")
@Controller
@RequestMapping(path = "/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserService userService;

    @Operation(summary = "Sign in user", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody SignInRequest signInRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUserName(), signInRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtService.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(AuthResponse.builder().id(userDetails.getId())
                        .userName(userDetails.getUsername())
                        .email(userDetails.getEmail())
                        .roles(roles)
                        .build());
    }


    @Operation(summary = "Sign up user", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiMessage> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) throws ApiAuthSpecificValidationException {
        boolean existsByUsername = userService.existsByUsername(signUpRequest.getUserName());
        boolean existsByEmail = userService.existsByEmail(signUpRequest.getEmail());
        if (!existsByUsername && !existsByEmail) {
            userService.createUser(signUpRequest);
            return ResponseEntity.ok().body(ApiMessage.builder().message("User registered successfully!").build());
        }

        List<ApiFieldError> errors = new ArrayList<>();
        if (existsByUsername) {
            errors.add(new ApiFieldError("userName", "Username is already taken!"));
        }
        if (existsByEmail) {
            errors.add(new ApiFieldError("email", "Email is already in use!"));
        }
        throw new ApiAuthSpecificValidationException("Validation error", errors);
    }

    @Operation(summary = "Sign out user", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @PostMapping("/signout")
    public ResponseEntity<ApiMessage> disconnectUser() {

        ResponseCookie jwtCookie = jwtService.getCleanJwtCookie();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(ApiMessage.builder().message("Vous avez été déconnecté !").build());
    }

    @Operation(summary = "Get current user", responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
    })
    @GetMapping("/current")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtService.getCleanJwtCookie().toString())
                    .body(AuthResponse.builder().id(null)
                            .userName("")
                            .email("")
                            .roles(List.of())
                            .build());
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(AuthResponse.builder().id(userDetails.getId())
                        .userName(userDetails.getUsername())
                        .email(userDetails.getEmail())
                        .roles(roles)
                        .build());
    }


}

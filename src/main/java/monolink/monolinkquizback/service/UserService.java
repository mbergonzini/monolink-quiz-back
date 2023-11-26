package monolink.monolinkquizback.service;

import jakarta.transaction.Transactional;
import monolink.monolinkquizback.auth.SignUpRequest;
import monolink.monolinkquizback.entity.ERole;
import monolink.monolinkquizback.entity.Role;
import monolink.monolinkquizback.entity.User;
import monolink.monolinkquizback.repository.RoleRepository;
import monolink.monolinkquizback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserService {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    public void createUser(SignUpRequest signUpRequest) {
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        User user = new User(signUpRequest.getUserName(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()), roles);

        userRepository.save(user);

    }

    public List<User> findAllWithParticipation() {
        return userRepository.findAllWithParticipation();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}

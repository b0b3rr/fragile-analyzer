import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceRobustTest {

    @Mock
    UserRepository userRepository;

    @Mock
    EmailSender emailSender;

    @InjectMocks
    UserService userService;

    @Test
    void shouldRegisterUser_returnSavedUser() {
        User inputUser = new User("test@example.com", "Test");
        when(userRepository.save(any(User.class))).thenReturn(inputUser);

        User result = userService.registerUser("test@example.com", "Test");

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("Test");
    }
}

class User {
    private String email;
    private String name;
    public User(String email, String name) { this.email = email; this.name = name; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}
interface UserRepository { User save(User user); }
interface EmailSender { void sendWelcomeEmail(User user); }
class UserService {
    private UserRepository repo;
    private EmailSender sender;
    public UserService(UserRepository repo, EmailSender sender) { this.repo = repo; this.sender = sender; }
    public User registerUser(String email, String name) {
        User u = new User(email, name);
        User saved = repo.save(u);
        sender.sendWelcomeEmail(saved);
        return saved;
    }
}

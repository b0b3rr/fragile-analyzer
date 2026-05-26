import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceFragileTest {

    @Mock
    UserRepository userRepository;

    @Mock
    EmailSender emailSender;

    @InjectMocks
    UserService userService;

    @Test
    void shouldRegisterUser() {
        User user = new User("test@example.com", "Test");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser("test@example.com", "Test");

        verify(userRepository).save(any(User.class));
        verify(emailSender).sendWelcomeEmail(user);

        assertEquals("test@example.com", result.getEmail());
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

package com.hcbs.service.auth;

import com.hcbs.dto.RegistrationRequest;
import com.hcbs.model.User;
import com.hcbs.model.UserRole;
import com.hcbs.model.UserStatus;
import com.hcbs.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:registration-service-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registersActiveCustomerWithNormalizedEmail() {
        User user = registrationService.registerCustomer(new RegistrationRequest(
                "diana",
                "Diana@Example.COM",
                "password1",
                "password1",
                "Diana Customer",
                "+44 7700 900123"
        ));

        assertThat(user.getUserId()).isNotNull();
        assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmail()).isEqualTo("diana@example.com");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(userRepository.findByUsername("diana")).isPresent();
    }

    @Test
    void rejectsDuplicateUsername() {
        registrationService.registerCustomer(request("dave1", "dave1@test.com"));
        assertThatThrownBy(() -> registrationService.registerCustomer(request("dave1", "dave2@test.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void rejectsDuplicateEmail() {
        registrationService.registerCustomer(request("erin", "erin@test.com"));
        assertThatThrownBy(() -> registrationService.registerCustomer(request("erin2", "erin@test.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void rejectsMismatchedPasswords() {
        assertThatThrownBy(() -> registrationService.registerCustomer(new RegistrationRequest(
                "frank",
                "frank@test.com",
                "password1",
                "password2",
                "Frank",
                null
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("do not match");
    }

    private static RegistrationRequest request(String username, String email) {
        return new RegistrationRequest(username, email, "password1", "password1", "Test User", null);
    }
}

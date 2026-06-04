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

/**
 * 注册服务自动化测试（成员 C）。
 * <p>
 * 覆盖客户注册校验与持久化；登录/三种角色侧栏为手工答辩用例。
 * 运行：{@code mvn test -Dtest=RegistrationServiceTest}
 */
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
    void registersActiveCustomerWithPhoneAndNormalizedEmail() {
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
        assertThat(user.getPhone()).isEqualTo("+447700900123");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(userRepository.findByUsername("diana")).isPresent();
    }

    @Test
    void registersCustomerWithoutEmail() {
        User user = registrationService.registerCustomer(new RegistrationRequest(
                "noemail",
                "",
                "password1",
                "password1",
                "No Email",
                "+44 7700 900124"
        ));

        assertThat(user.getEmail()).isNull();
        assertThat(user.getPhone()).isEqualTo("+447700900124");
    }

    @Test
    void rejectsDuplicateUsername() {
        registrationService.registerCustomer(request("dave1", "dave1@test.com", "+44 7700 900125"));
        assertThatThrownBy(() -> registrationService.registerCustomer(request("dave1", "dave2@test.com", "+44 7700 900126")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void rejectsDuplicateEmail() {
        registrationService.registerCustomer(request("erin", "erin@test.com", "+44 7700 900127"));
        assertThatThrownBy(() -> registrationService.registerCustomer(request("erin2", "erin@test.com", "+44 7700 900128")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void rejectsMissingPhone() {
        assertThatThrownBy(() -> registrationService.registerCustomer(new RegistrationRequest(
                "nophone",
                null,
                "password1",
                "password1",
                "No Phone",
                null
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number");
    }

    @Test
    void rejectsDuplicatePhone() {
        registrationService.registerCustomer(request("phone1", null, "+44 7700 900129"));

        assertThatThrownBy(() -> registrationService.registerCustomer(request("phone2", null, "+44 7700 900129")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone number");
    }

    @Test
    void rejectsMismatchedPasswords() {
        assertThatThrownBy(() -> registrationService.registerCustomer(new RegistrationRequest(
                "frank",
                "frank@test.com",
                "password1",
                "password2",
                "Frank",
                "+44 7700 900130"
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("do not match");
    }

    private static RegistrationRequest request(String username, String email, String phone) {
        return new RegistrationRequest(username, email, "password1", "password1", "Test User", phone);
    }
}

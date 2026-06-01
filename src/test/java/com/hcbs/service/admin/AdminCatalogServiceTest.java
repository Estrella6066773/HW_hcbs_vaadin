package com.hcbs.service.admin;

import com.hcbs.model.User;
import com.hcbs.model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-catalog-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminCatalogServiceTest {

    @Autowired
    private AdminCatalogService adminCatalogService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void filtersCustomerAccountsByPhonePrefix() {
        signInAsAdmin();

        assertThat(adminCatalogService.listUsersByCustomerPhone("")).hasSize(9);

        List<User> customers = adminCatalogService.listUsersByCustomerPhone("1380013800");

        assertThat(customers)
                .hasSize(3)
                .allSatisfy(user -> {
                    assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
                    assertThat(user.getPhone()).startsWith("1380013800");
                });
    }

    @Test
    void phoneSearchDoesNotReturnEmployeeAccounts() {
        signInAsAdmin();

        assertThat(adminCatalogService.listUsersByCustomerPhone("138003")).isEmpty();
    }

    private void signInAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("13800338001", "demo", List.of()));
    }
}

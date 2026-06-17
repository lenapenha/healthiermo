package org.healthiermo.homepage.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private SecurityConfig config;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.user.name", () -> "admin");
        registry.add("spring.security.user.password", () -> "$2a$10$j7crAVi5zkOmpCz4a54Oc.xJJkJa5L90Rjtj4DoUZkdgfM.gq4zFK");
    }

    @Test
    void passwordEncoderShouldNotBeNull() {
        assertNotNull(passwordEncoder);
    }

    @Test
    void passwordEncoderShouldEncodePasswords() {
        String rawPassword = "password";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void userDetailsServiceShouldNotBeNull() {
        assertNotNull(userDetailsService);
    }

    @Test
    void userDetailsServiceShouldLoadUserByUsername() {
        UserDetails user = userDetailsService.loadUserByUsername("admin");

        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void userDetailsServiceShouldEncodePassword() {
        UserDetails user = userDetailsService.loadUserByUsername("admin");

        assertNotNull(user.getPassword());
        assertNotEquals("testpass", user.getPassword());
        assertTrue(passwordEncoder.matches("testpass", user.getPassword()));
    }
}

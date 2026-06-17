package org.healthiermo.homepage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileUploadIntegrationTest {

    private static final long FIFTEEN_MB = 15L * 1024 * 1024;

    @TempDir
    static Path tempDir;

    @LocalServerPort
    private int port;

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.public-data-root", () -> tempDir.toString());
        registry.add("spring.security.user.name", () -> "admin");
        registry.add("spring.security.user.password", () -> "$2a$10$j7crAVi5zkOmpCz4a54Oc.xJJkJa5L90Rjtj4DoUZkdgfM.gq4zFK");
    }

    @Test
    void upload15MbFileWritesToTempDirectory() {
        byte[] content = new byte[(int) FIFTEEN_MB];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("sections[0].file", new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return "test.wav";
            }
        });
        body.add("sections[0].sectionKey", "OUTER_RING");
        body.add("sections[0].title", "Test Title");
        body.add("sections[0].text", "Test text");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBasicAuth("admin", "testpass");
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/save/misc", request, String.class);

        assertEquals(200, response.getStatusCode().value(), "Expected upload to succeed");

        Path expected = tempDir.resolve("audio-files").resolve("MISC-OUTER_RING.wav");
        assertTrue(Files.exists(expected), "Uploaded file should exist in temp directory: " + expected);
        try {
            assertEquals(FIFTEEN_MB, Files.size(expected),
                    "File size should match upload size exactly");
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

}

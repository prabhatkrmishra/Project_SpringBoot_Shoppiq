package com.pkmprojects.shoppiq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "shoppiq.ai.enabled=false",
    "AI_NVIDIA_API_KEY=test-dummy-key-for-startup"
})
class ShoppiqApplicationTests {

	@Test
	void contextLoads() {
	}

}

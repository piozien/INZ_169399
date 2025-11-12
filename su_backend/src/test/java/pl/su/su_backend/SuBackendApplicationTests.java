package pl.su.su_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import pl.su.su_backend.testsupport.OAuth2TestConfig;

@SpringBootTest(properties = "app.scheduling.enabled=false")
@ActiveProfiles("test")
@Import(OAuth2TestConfig.class)
class SuBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}

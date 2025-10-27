package pl.su.su_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "app.scheduling.enabled=false")
@ActiveProfiles("test")
class SuBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}

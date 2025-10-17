package pl.su.su_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.scheduling.enabled=false")
class SuBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}

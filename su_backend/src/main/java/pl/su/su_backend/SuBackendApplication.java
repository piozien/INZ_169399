package pl.su.su_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SuBackendApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SuBackendApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
	}

}

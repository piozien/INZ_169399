package pl.su.su_backend;

import org.springframework.boot.SpringApplication;

public class TestSuBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(SuBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

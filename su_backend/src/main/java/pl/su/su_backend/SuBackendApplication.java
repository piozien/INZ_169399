package pl.su.su_backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SuBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuBackendApplication.class, args);
	}

    @PostConstruct
    public void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"));
    }
}

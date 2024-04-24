package co.edu.unipamplona.ciadti.cargatrabajo.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CargaTrabajoServicesApplication {
	public static void main(String[] args) {
		SpringApplication.run(CargaTrabajoServicesApplication.class, args);
	}
}

package br.edu.ucsal.campus;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class CampusApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusApiApplication.class, args);
	}

	@Bean 
	ApplicationRunner listarRotas(RequestMappingHandlerMapping mapping) {
		return args -> {
			mapping.getHandlerMethods().forEach((rota, metodo) -> {
				System.out.println(rota + " " + metodo);
			});
		};
	}
}

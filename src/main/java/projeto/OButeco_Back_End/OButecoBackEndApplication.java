package projeto.OButeco_Back_End;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OButecoBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(OButecoBackEndApplication.class, args);
	}

}

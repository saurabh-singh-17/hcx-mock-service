package org.swasth.hcx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class HCXApplication {

	public static void main(String[] args) {
		SpringApplication.run(HCXApplication.class, args);
	}

}

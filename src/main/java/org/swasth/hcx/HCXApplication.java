package org.swasth.hcx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = "org/swasth/hcx")
public class HCXApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(HCXApplication.class, args);
	}

}

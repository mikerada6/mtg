package org.rezatron.rezatron_mtg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class RezatronMtgApplication {

	public static void main(String[] args) {
		SpringApplication.run(RezatronMtgApplication.class, args);
	}

}

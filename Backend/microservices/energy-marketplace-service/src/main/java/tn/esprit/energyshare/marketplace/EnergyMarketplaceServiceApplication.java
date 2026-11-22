package tn.esprit.energyshare.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class EnergyMarketplaceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnergyMarketplaceServiceApplication.class, args);
	}

}

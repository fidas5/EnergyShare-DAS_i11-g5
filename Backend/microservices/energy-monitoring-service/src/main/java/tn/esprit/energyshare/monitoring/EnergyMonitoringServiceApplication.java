package tn.esprit.energyshare.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@EnableDiscoveryClient
@SpringBootApplication
public class EnergyMonitoringServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnergyMonitoringServiceApplication.class, args);
	}

}

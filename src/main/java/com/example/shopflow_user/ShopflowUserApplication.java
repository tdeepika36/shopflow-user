package com.example.shopflow_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
public class ShopflowUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopflowUserApplication.class, args);
	}

}

package com.phoenix.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.phoenix"})
public class PhoenixAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhoenixAgentApplication.class, args);
		log.info("##############智能体平台启动成功###############");
	}

}

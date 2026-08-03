package com.zenticode.medical;

import org.springframework.boot.SpringApplication;

public class TestMedicalApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(MedicalApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

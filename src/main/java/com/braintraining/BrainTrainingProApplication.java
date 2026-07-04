package com.braintraining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

@SpringBootApplication
public class BrainTrainingProApplication {

    public static void main(String[] args) {
        System.out.println(SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8().encode("admin123"));
        SpringApplication.run(BrainTrainingProApplication.class, args);
    }
}
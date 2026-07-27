package com.umss.sigesa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.umss.sigesa.adapter",
        "com.umss.sigesa.config"
})
@EntityScan(basePackages = "com.umss.sigesa.adapter.out.persistance.entity")
@EnableJpaRepositories(basePackages = "com.umss.sigesa.adapter.out.persistance")
public class SigesaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigesaApplication.class, args);
    }

}

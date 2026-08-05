package com.umss.sigesa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {
        "com.umss.sigesa.adapter",
        "com.umss.sigesa.config"
})
public class SigesaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigesaApplication.class, args);
    }

}

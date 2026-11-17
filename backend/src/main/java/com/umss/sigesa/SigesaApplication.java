package com.umss.sigesa;

import com.umss.sigesa.extraction.NotificationServiceApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {
        "com.umss.sigesa.adapter",
        "com.umss.sigesa.config"
})
public class SigesaApplication {

    public static void main(String[] args) {
        String app = System.getenv().getOrDefault("SIGESA_APP", "api");
        if ("notification-service".equalsIgnoreCase(app)) {
            SpringApplication.run(NotificationServiceApplication.class, args);
            return;
        }
        SpringApplication.run(SigesaApplication.class, args);
    }

}

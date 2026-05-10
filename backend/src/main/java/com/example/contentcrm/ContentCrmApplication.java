package com.example.contentcrm;

import com.example.contentcrm.dataaccess.file.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(FileStorageProperties.class)
public class ContentCrmApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentCrmApplication.class, args);
    }
}

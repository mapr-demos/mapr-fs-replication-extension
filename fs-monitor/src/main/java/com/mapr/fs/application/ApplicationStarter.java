package com.mapr.fs.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.apache.log4j.Logger;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan("com.mapr.fs.*")
public class ApplicationStarter {

    private static final Logger log = Logger.getLogger(ApplicationStarter.class);

    public static void main(String[] args) {
        start(args);
    }

    private static void start(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ApplicationStarter.class, args);

        log.info("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            log.info(beanName);
        }
    }

}

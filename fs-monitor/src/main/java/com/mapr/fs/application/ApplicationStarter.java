package com.mapr.fs.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan("com.mapr.fs.controllers")
public class ApplicationStarter {
    
    public static void main(String[] args) {
        start(args);
    }

    private static void start(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ApplicationStarter.class, args);

        System.out.println("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

}

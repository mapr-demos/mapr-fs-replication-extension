package com.mapr.fs.application;

import com.mapr.fs.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
@ComponentScan("com.mapr.fs.*")
public class ApplicationStarter {


    public static void main(String[] args) throws ParseException {
        BasicConfigurator.configure();
        try {
            ConfigUtil.setConfigPath(args);
            start(args);
        } catch (Exception ex) {
            log.error(ex.toString());
        }
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

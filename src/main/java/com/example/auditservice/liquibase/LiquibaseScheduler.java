package com.example.auditservice.liquibase;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class LiquibaseScheduler {
    private final ResourceLoader resourceLoader;
    private final DataSource dataSource;

    public LiquibaseScheduler(ResourceLoader resourceLoader, DataSource dataSource) {
        this.resourceLoader = resourceLoader;
        this.dataSource = dataSource;
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void runRotationChangelog() throws LiquibaseException, IOException {
        log.info("Running rotationChangelog");
        String dateSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_hh_mm_ss"));
        String templatePath = "classpath:db/changelog/template/rotate-logs-template.yaml";
        String outputPath = "classpath:db/changelog/generated/rotate-logs-" + dateSuffix + ".yaml";

        // 1. Load and fill template
        Resource resource = resourceLoader.getResource(templatePath);
        String template = new String(resource.getInputStream().readAllBytes());
        String filled = template.replace("${date}", dateSuffix);

        // 2. Write to generated folder
        Path path = Paths.get(outputPath);
        Path path1 = Files.createDirectories(path.getParent());
        Path path2 = Files.writeString(path, filled);


        // Run the change log using liquibase
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog("classpath:db/changelog/generated/rotate-logs-" + dateSuffix + ".yaml");
        springLiquibase.afterPropertiesSet();
    }
}

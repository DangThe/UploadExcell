package com.vrbank.uploadexcel.config;

import java.util.List;
import java.util.concurrent.Executor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * File Upload Configuration
 */
@Configuration
@EnableAsync
public class FileUploadConfig implements WebMvcConfigurer {

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    @ConfigurationProperties(prefix = "upload.excel")
    public ExcelUploadProperties excelUploadProperties() {
        return new ExcelUploadProperties();
    }

    @Bean(name = "excelUploadTaskExecutor")
    public Executor excelUploadTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ExcelUpload-");
        executor.initialize();
        return executor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/api/excel-upload/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    /**
     * Excel Upload Properties
     */
    public static class ExcelUploadProperties {

        private String maxFileSize = "50MB";
        private List<String> allowedExtensions = List.of("xlsx", "xls");
        private String tempDirectory = System.getProperty("java.io.tmpdir") + "/excel-uploads";
        private int maxRowsPerBatch = 10000;

        // Getters and Setters
        public String getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public List<String> getAllowedExtensions() {
            return allowedExtensions;
        }

        public void setAllowedExtensions(List<String> allowedExtensions) {
            this.allowedExtensions = allowedExtensions;
        }

        public String getTempDirectory() {
            return tempDirectory;
        }

        public void setTempDirectory(String tempDirectory) {
            this.tempDirectory = tempDirectory;
        }

        public int getMaxRowsPerBatch() {
            return maxRowsPerBatch;
        }

        public void setMaxRowsPerBatch(int maxRowsPerBatch) {
            this.maxRowsPerBatch = maxRowsPerBatch;
        }
    }
}

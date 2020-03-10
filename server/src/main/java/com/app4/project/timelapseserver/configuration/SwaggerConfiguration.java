package com.app4.project.timelapseserver.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

  @Bean
  ApiInfo info() {
    return new ApiInfo(
      "Timelapse Server",
      "API to handle an email app",
      "1.0",
      "Terms of service",
      new Contact("FONKOUA Tambue Nelson", "no URL", "tambue@hotmail.fr"),
      "MIT License", "some url", Collections.emptyList());
  }

  @Bean
  Docket api(ApiInfo info) {
    return new Docket(DocumentationType.SWAGGER_2)
      .select()
      .apis(RequestHandlerSelectors.any())
      .paths(PathSelectors.any())
      .build()
      .apiInfo(info);
  }

  @Primary
  @Bean
  public SwaggerResourcesProvider swaggerResourcesProvider(
    InMemorySwaggerResourcesProvider defaultResourcesProvider) {
    return () -> {
      SwaggerResource wsResource = new SwaggerResource();
      wsResource.setName("Swagger");
      wsResource.setSwaggerVersion("2.0");
      wsResource.setLocation("/swagger.yaml");
      return List.of(wsResource);
    };
  }

}

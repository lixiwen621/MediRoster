package com.mediroster.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI 描述。
 *
 * @author tongguo.li
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medirOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediRoster 排班 API")
                        .description("检验科排班主数据与周排班接口；统一返回 ApiResponse。")
                        .version("v1"));
    }
}

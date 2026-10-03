package com.gymbooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gymBookingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Gym Booking API")
                .version("1.0.0")
                .description("Schedule gym classes, register members and manage bookings with capacity rules."));
    }
}

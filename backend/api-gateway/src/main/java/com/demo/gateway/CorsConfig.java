package com.demo.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * FOR react refresh:37 Download the React DevTools for a better development
 * experience: https://react.dev/link/react-devtools login:1 Access to
 * XMLHttpRequest at 'http://localhost:8080/api/users/login' from origin
 * 'http://localhost:3000' has been blocked by CORS policy: Response to
 * preflight request doesn't pass access control check: No
 * 'Access-Control-Allow-Origin' header is present on the requested resource.
 * .....
 */
@Configuration
public class CorsConfig {

	@Bean
	CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOrigin("http://localhost:3000");
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}
}

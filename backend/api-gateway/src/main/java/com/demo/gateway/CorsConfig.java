package com.demo.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * FOR react refresh:37 Download the React DevTools for a better development
 * experience: <a href="https://react.dev/link/react-devtools">https://react.dev/link/react-devtools</a> login:1 Access to
 * XMLHttpRequest at <code>'http://localhost:8080/api/users/login'</code> from origin
 * <code>'http://localhost:3000'</code> has been blocked by CORS policy: Response to
 * preflight request doesn't pass access control check: No
 * 'Access-Control-Allow-Origin' header is present on the requested resource.
 * .....
 */
@Configuration
public class CorsConfig {

	@Bean
	CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOrigin("http://localhost:3001");
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}
}

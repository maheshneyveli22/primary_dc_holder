
package com.expd.expo.booking.security.authentication;

import com.expd.expo.booking.config.AppConfiguration;
import com.expd.expo.booking.security.authentication.custom.CustomJwtAuthenticationTokenConverter;
import com.sun.org.apache.xerces.internal.parsers.SecurityConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import javax.security.auth.message.AuthException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configure security for Users and Admins.
 * 06/18/2019 (chq-emmam) - all security configurations moved into this class for consolidation.
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MultiSecurityConfig {

    public static class AuthenticationEntryPointResponse implements AuthenticationEntryPoint {
        @Override
        public void commence( HttpServletRequest req, HttpServletResponse res, AuthenticationException e)
                throws IOException {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write(new AuthException().toString());
        }
    }


    /**
     * These are the default security settings that must be deployed to production. This requires a valid
     * JWT token for most endpoints. It is behind {@link SecurityConfigAllowInvalid} in the order due to the way
     * that spring views and reads the @ConditionalOnProperty annotation.
     *
     * This configuration is on conditionally. ONE of {@link SecurityConfig}, {@link SecurityConfigAllowInvalid},
     * and {@link SecurityConfigNoAuthentication} must be turned on via the properties listed on them.
     */
    @ConditionalOnProperty("security.enable-authorization")
    @Configuration
    @Order(2)
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
    public static class SecurityConfig extends SecurityConfiguration {

        private AppConfiguration.Security security;


        @Autowired
        public SecurityConfig(AppConfiguration config) {
            security = config.getSecurity();
        }

//        Commenting the below code as BookingTokenCheck Fails
//        @Bean
//        public AuthenticationManager authenticationManager( AuthenticationConfiguration authenticationConfiguration) throws Exception {
//            return authenticationConfiguration.getAuthenticationManager();
//        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            auth.authenticationProvider(provider);
        }

        @Bean
        public SecurityFilterChain filterChain( HttpSecurity http) throws Exception {
            http
                    .headers()
                    .contentSecurityPolicy("frame-ancestors")
                    .and()
                    .frameOptions()
                    .deny()
                    .and()
                    .cors().and()
                    .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow CORS option calls from Angular
                    .antMatchers(HttpMethod.GET, "/graphql/schema.json").denyAll() // do not require security on public graphql schema
                    .antMatchers(HttpMethod.POST, "/reset-password", "/contact-support").permitAll()
                    .antMatchers(HttpMethod.POST, "/mails/verification").permitAll() // verification email request is unauthenticated
                    .antMatchers(HttpMethod.GET, "/health-check").permitAll()
                    .antMatchers(HttpMethod.POST, "/reporting/**").permitAll() // fallback for sockjs
                    .antMatchers(HttpMethod.GET, "/reporting/**").permitAll()
                    .antMatchers(HttpMethod.POST, "/booking/**").permitAll() // fallback for sockjs
                    .antMatchers(HttpMethod.GET, "/booking/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/hystrix/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/hystrix.stream/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/tokencheck").permitAll()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .oauth2ResourceServer().jwt()
                    .jwtAuthenticationConverter(new CustomJwtAuthenticationTokenConverter())
                    .and()
                    .authenticationEntryPoint(new AuthenticationEntryPointResponse()).and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            return http.build();
        }
        @Bean
        JwtDecoder jwtDecoder() {
            NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                    JwtDecoders.fromOidcIssuerLocation(security.getIssuer());

            OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(security.getApiAudience());
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(security.getIssuer());
            OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

            jwtDecoder.setJwtValidator(withAudience);

            return jwtDecoder;
        }
    }

    /**
     * Mock security matcher that will skip validating an auth token if the header 'Ignore-Auth-Validation'
     * is present.  (Useful in automated tests)
     *
     * This configuration is on conditionally. ONE of {@link SecurityConfig}, {@link SecurityConfigAllowInvalid},
     * and {@link SecurityConfigNoAuthentication} must be turned on via the properties listed on them.
     */
    @ConditionalOnProperty({"security.allow-invalid-tokens", "security.enable-authorization"})
    @Component
    @Order(1)
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
    public class SecurityConfigAllowInvalid {

        private AppConfiguration.Security security;

        @Autowired
        public SecurityConfigAllowInvalid(AppConfiguration config) {
            security = config.getSecurity();
        }

//        Commenting the below code as BookingTokenCheckFails
//        @Bean
//        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//            return authenticationConfiguration.getAuthenticationManager();
//        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            auth.authenticationProvider(provider);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

            http
                    .headers()
                    .contentSecurityPolicy("frame-ancestors")
                    .and()
                    .frameOptions()
                    .deny()
                    .and()
                    .cors().and()
                    .requestMatcher(in -> in.getHeader("Ignore-Auth-Validation") != null &&
                            in.getHeader("Ignore-Auth-Validation").equals("true"))
                    .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow CORS option calls from Angular
                    .antMatchers(HttpMethod.GET, "/graphql/schema.json").denyAll() // do not require security on public graphql schema
                    .antMatchers(HttpMethod.POST, "/mails/verification").permitAll() // verification email request is unauthenticated
                    .antMatchers(HttpMethod.GET, "/health-check").permitAll()
                    .antMatchers(HttpMethod.POST, "/reporting/**").permitAll() // fallback for sockjs
                    .antMatchers(HttpMethod.GET, "/reporting/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/tokencheck").permitAll()
                    .anyRequest()
                    .authenticated().and()
                    .oauth2ResourceServer().jwt()
                    .jwtAuthenticationConverter(new CustomJwtAuthenticationTokenConverter())
                    .and()
                    .authenticationEntryPoint ( new AuthenticationEntryPointResponse()).and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            return http.build ();
        }
        @Bean
        JwtDecoder jwtDecoder() {
            NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                    JwtDecoders.fromOidcIssuerLocation(security.getIssuer());

            OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(security.getApiAudience());
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(security.getIssuer());
            OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

            jwtDecoder.setJwtValidator(withAudience);

            return jwtDecoder;
        }
    }
}


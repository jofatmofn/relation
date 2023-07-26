package org.sakuram.relation.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakuram.relation.util.AppException;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class AppWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.authorizeRequests(a -> {
			try { a
				.antMatchers("/*", "/**/*").permitAll()
				.anyRequest().authenticated()
				.and()
				.csrf().disable();	// TODO: CSRF to be enabled
				} catch (Exception e1) {
					throw new AppException("Exception during authentication",e1);
				}
			})
		.exceptionHandling(e -> e
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
		.logout(l -> l
				.logoutSuccessUrl("/").permitAll()
				.invalidateHttpSession(false))
		.oauth2Login()
        .successHandler(new AuthenticationSuccessHandler() {
        	 
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
            	response.sendRedirect(request.getContextPath() + request.getServletPath().substring(request.getServletPath().lastIndexOf("/")));
            	// TODO: Handle default/no context scenario
            }
        });
		
	}

}

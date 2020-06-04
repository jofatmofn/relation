package org.sakuram.relation;

import org.sakuram.relation.util.AppException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@SpringBootApplication
public class RelationApplication extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests(a -> {
				try {
					a
						.antMatchers("/*", "/**/*").permitAll()
						.anyRequest().authenticated()
					    .and()
					    .csrf().disable();	// TODO: CSRF to be enabled
				} catch (Exception e1) {
					throw new AppException("Exception during authentication",e1);
				}
			}
			)
			.exceptionHandling(e -> e
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
			)
	        .logout(l -> l
	                .logoutSuccessUrl("/").permitAll()
	        )
			.oauth2Login();
	}

	public static void main(String[] args) {
		SpringApplication.run(RelationApplication.class, args);
	}

}

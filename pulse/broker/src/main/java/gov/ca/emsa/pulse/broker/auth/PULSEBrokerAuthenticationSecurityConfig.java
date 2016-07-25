package gov.ca.emsa.pulse.broker.auth;

import java.util.ArrayList;
import java.util.List;

import gov.ca.emsa.pulse.auth.authentication.JWTUserConverter;
import gov.ca.emsa.pulse.auth.filter.JWTAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebSecurity
@Configuration
public class PULSEBrokerAuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {
	
	public PULSEBrokerAuthenticationSecurityConfig() {
		super(true);
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/health**");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		//user has been authenticated through the public service before they come here
		//so we can allow everyone
			.authorizeRequests()
			.antMatchers("/**")
			.permitAll()
			//.hasRole("USER")
		.and()
			.addFilterBefore(new HttpRequestUserFilter(), UsernamePasswordAuthenticationFilter.class)
			.headers();
	}
	
	@Bean
	public MappingJackson2HttpMessageConverter jsonConverter(){
		
		MappingJackson2HttpMessageConverter bean = new MappingJackson2HttpMessageConverter();
		
		bean.setPrefixJson(false);
		
		List<MediaType> mediaTypes = new ArrayList<MediaType>();
		mediaTypes.add(MediaType.APPLICATION_JSON);
		
		bean.setSupportedMediaTypes(mediaTypes);
		
		return bean;
	}
	
}
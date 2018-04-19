package gov.ca.emsa.pulse.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import gov.ca.emsa.pulse.auth.authentication.JWTUserConverter;
import gov.ca.emsa.pulse.auth.filter.JWTAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Configuration
@ComponentScan({
        "gov.ca.emsa.pulse.auth.**"
})
public class PULSEAuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JWTUserConverter userConverter;

    public PULSEAuthenticationSecurityConfig() {
        super(true);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/health**");
        web.ignoring().antMatchers("/api-docs**");
        web.ignoring().antMatchers("/patientDiscovery");
        web.ignoring().antMatchers("/documentQuery");
        web.ignoring().antMatchers("/retrieveDocumentSet");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().antMatchers("/**").requiresSecure().and()
                .addFilterBefore(new JWTAuthenticationFilter(userConverter), UsernamePasswordAuthenticationFilter.class)
                .headers();
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {

        MappingJackson2HttpMessageConverter bean = new MappingJackson2HttpMessageConverter();

        bean.setPrefixJson(false);

        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);

        bean.setSupportedMediaTypes(mediaTypes);

        return bean;
    }

}

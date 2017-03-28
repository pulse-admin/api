package gov.ca.emsa.pulse.auth.filter;

import gov.ca.emsa.pulse.auth.authentication.JWTUserConverter;
import gov.ca.emsa.pulse.auth.json.ErrorJSONObject;
import gov.ca.emsa.pulse.auth.jwt.JWTValidationException;
import gov.ca.emsa.pulse.auth.user.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JWTAuthenticationFilter extends GenericFilterBean {

	private JWTUserConverter userConverter;
	List<String> exemptions;
	ObjectMapper jsonMapper;

	public JWTAuthenticationFilter(JWTUserConverter userConverter){
		this.userConverter = userConverter;
		exemptions = new ArrayList<String>();
		exemptions.add("/patientDiscovery");
		//TODO: add others here

		jsonMapper = new ObjectMapper();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;

		String authorizationHeader = request.getHeader("Authorization");

		if(exemptions.contains(request.getRequestURI().trim())) {
			chain.doFilter(req, res);
		} else {
			if (authorizationHeader == null){
				ErrorJSONObject errorObj = new ErrorJSONObject("No header");
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(errorObj);
				res.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
				chain.doFilter(req, res); //continue
				SecurityContextHolder.getContext().setAuthentication(null);
			} else {

				User authenticatedUser;
				String jwt = null;

				try {
					jwt = authorizationHeader.split(" ")[1];
				} catch (java.lang.ArrayIndexOutOfBoundsException e){
					ErrorJSONObject errorObj = new ErrorJSONObject("Token must be presented in the form: Bearer token");
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String json = ow.writeValueAsString(errorObj);
					res.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
				}

				if (jwt != null){

					try {
						authenticatedUser = userConverter.getAuthenticatedUser(jwt);
						SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
						chain.doFilter(req, res); //continue
						SecurityContextHolder.getContext().setAuthentication(null);

					} catch (JWTValidationException e) {

						ErrorJSONObject errorObj = new ErrorJSONObject(e.getMessage());
						ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
						String json = ow.writeValueAsString(errorObj);
						res.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));

					}
				}
			}
		}
	}
}

package gov.ca.emsa.pulse.broker.auth;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.service.InvalidArgumentsException;
import gov.ca.emsa.pulse.service.UserUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

public class AcfLastAccessFilter extends GenericFilterBean {
	private static final Logger logger = LogManager.getLogger(AcfLastAccessFilter.class);
	@Autowired private AlternateCareFacilityManager acfManager;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		CommonUser user = UserUtil.getCurrentUser();
		if(user != null) {
			//update last read time for the acf in the header
			/*if(user != null && user.getAcf() != null 
					&& user.getAcf().getId() != null) {*/
			if (user.getLiferayAcfId() != null) {
				AlternateCareFacilityDTO acf = acfManager.getByLiferayAcfId(user.getLiferayAcfId());
				//AlternateCareFacilityDTO acf = acfManager.getById(user.getAcf().getId());
				if(acf != null) {
					//the ACF could have been deleted between the last time a request
					//was made using it and this request... so have to check for null
					acf.setLastReadDate(new Date());
					try {
						acfManager.updateLastModifiedDate(acf.getId());
					} catch(SQLException ex) {
						throw new ServletException(ex.getMessage());
					}
//				} else {
//					//logger.error("ACF with ID " + user.getAcf().getId() + " and identifier " + user.getAcf().getIdentifier() + " does not exist!");
//					logger.error("ACF with ID " + user.getLiferayAcfId() +  " does not exist!");
//					throw new ServletException("ACF " + user.getLiferayAcfId() + " does not exist!");
				}
			}
		} else {
			logger.error("There was no user object in the current security context.");
		}
		
		chain.doFilter(req, res);
	}

	public AlternateCareFacilityManager getAcfManager() {
		return acfManager;
	}

	public void setAcfManager(AlternateCareFacilityManager acfManager) {
		this.acfManager = acfManager;
	}

//	@Override
//	public void init(FilterConfig filterConfig) throws ServletException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void destroy() {
//		// TODO Auto-generated method stub
//		
//	}
}

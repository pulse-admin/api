package gov.ca.emsa.pulse.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.dto.DtoToDomainConverter;
import gov.ca.emsa.pulse.broker.dto.LocationDTO;
import gov.ca.emsa.pulse.broker.manager.AuditEventManager;
import gov.ca.emsa.pulse.broker.manager.LocationManager;
import gov.ca.emsa.pulse.common.domain.Location;
import gov.ca.emsa.pulse.common.domain.stats.LocationStatistics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "locations")
@RestController
@RequestMapping("/locations")
public class LocationService {
    @Autowired
    LocationManager locationManager;
    @Autowired
    AuditEventManager auditManager;

    @ApiOperation(value = "Get the list of locations")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN"
    })
    public List<Location> getAll() {
        CommonUser user = UserUtil.getCurrentUser();
        // auditManager.addAuditEventEntry(QueryType.GET_ALL_ORGANIZATIONS,
        // "/locations", user.getSubjectName());
        List<LocationDTO> locationDtos = locationManager.getAll();
        List<Location> locations = new ArrayList<Location>();
        for (LocationDTO locationDto : locationDtos) {
            Location location = DtoToDomainConverter.convert(locationDto);
            locations.add(location);
        }
        return locations;
    }

    @ApiOperation(
            value = "Get dynamically calculated statistics on how quickly each location is responding to requests. "
                    + "Either startDate, endDate, or both may be null.")
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN"
    })
    public List<LocationStatistics> getPatientDiscoveryRequestStatistics(
            @RequestParam(name = "start", required = false) Long startMillis,
            @RequestParam(name = "end", required = false) Long endMillis) {
        Date startDate = null;
        if (startMillis != null) {
            startDate = new Date(startMillis);
        }
        Date endDate = null;
        if (endMillis != null) {
            endDate = new Date(endMillis);
        }
        return locationManager.getPatientDiscoveryRequestStatistics(startDate, endDate);
    }
}

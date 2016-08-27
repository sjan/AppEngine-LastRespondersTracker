package org.lastresponders.tracker.controller;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;

import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.data.TripStatus;
import org.lastresponders.tracker.exception.BadDataException;
import org.lastresponders.tracker.exception.NoDataException;
import org.lastresponders.tracker.service.JourneyService;
import org.lastresponders.tracker.service.PollingService;

@Path("/journey/")
public class TripResource {
	private static final Logger log = Logger.getLogger(TripResource.class.getName());
	private String journeyId = "projectSpark";

	@Inject
	JourneyService journeyService;

	@Inject
	PollingService pollingService;

	@GET
	@Path("/test")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String testMethod() throws NoDataException {
		pollingService.resampleRoute(journeyId);
		return "ok";
	}

	@GET
	@Path("/plannedRoute")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<TripPosition> plannedRoute() throws NoDataException, BadDataException {
		return journeyService.plannedRoute(journeyId);
	}

	@GET
	@Path("/plannedRouteRefresh")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String refreshSheets() throws BadDataException {
		pollingService.refreshPlannedRoute(journeyId);
		return "ok";
	}

	@GET
	@Path("/plannedStatus")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public TripStatus plannedStatus() throws BadDataException, NoDataException {
		return journeyService.plannedStatus(journeyId);
	}

	@GET
	@Path("/progressRoute")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<TripPosition> progressRoute() throws NoDataException {
		return journeyService.progressRoute(journeyId);
	}

	@GET
	@Path("/progressStatus")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public TripStatus progressStatus() throws NoDataException {
		return journeyService.progressStatus(journeyId);
	}

	@GET
	@Path("/progressPosition")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public TripPosition progressPosition() throws NoDataException {
		return journeyService.progressPosition(journeyId);
	}

	@GET
	@Path("/pollDatasource")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String pollDatasources() {
		pollingService.pollGpsPoints(journeyId);
		return "ok";
	}

	@GET
	@Path("/pollResample")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String pollResample() throws NoDataException {
		pollingService.pollResampleRoute(journeyId);
		return "ok";
	}
	

	@GET
	@Path("/routeCacheRefresh")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String refreshRoute() throws BadDataException {
		pollingService.refreshProgressRoute(journeyId);
		return "ok";
	}
}

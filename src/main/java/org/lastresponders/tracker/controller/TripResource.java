package org.lastresponders.tracker.controller;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;

import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.service.JourneyService;
import org.lastresponders.tracker.service.PollingService;

@Path("/journey/")
public class TripResource {
	private static final Logger log = Logger.getLogger(TripResource.class.getName());

	@Inject
	JourneyService journeyService;

	@Inject
	PollingService pollingService;

	@GET
	@Path("/test")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String testMethod() {
		log.info("test");
		return "ok";
	}

	@GET
	@Path("/plannedRoute")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<TripPosition> plannedRoute() {
		return journeyService.plannedRoute();
	}

	@GET
	@Path("/progressRoute")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<TripPosition> progressRoute() {
		return journeyService.progressRoute();
	}

	@GET
	@Path("/poll")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String pollDatasources() {
		pollingService.poll();
		return "ok";
	}
}

package org.lastresponders.tracker.controller;

import java.util.Calendar;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;

import org.lastresponders.tracker.data.TestObject;
import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.service.JourneyService;
import org.lastresponders.tracker.service.TestService;


@Path("/journey/")
public class TripResource {
	@Inject
	TestService testService;

	@Inject
	JourneyService journeyService;

	@GET	 
	@Path("/test")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public TestObject testMethod()  {	
		return new TestObject(testService.testCall(), Calendar.getInstance().getTime());
	}
	
	@GET	 
	@Path("/plannedRoute")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List <TripPosition> plannedRoute()  {	
		return journeyService.plannedRoute();
	}

}


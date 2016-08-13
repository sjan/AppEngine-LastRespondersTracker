package org.lastresponders.tracker.controller;

import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.lastresponders.tracker.data.TestObject;


@Path("/journey/")
public class TripResource {
	
	@Context
	ServletContext context;
	
	@GET	 
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public TestObject testMethod()  {	
		return new TestObject("some String", Calendar.getInstance().getTime());
	}

}


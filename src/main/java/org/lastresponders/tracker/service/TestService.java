package org.lastresponders.tracker.service;

import java.io.IOException;
import java.util.logging.Logger;

public class TestService {
	private static final Logger log = Logger.getLogger(TestService.class
			.getName());
	
	SheetsTestService tes = new SheetsTestService();
	
	public String testCall() {
		try {
			tes.test();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("serviceCall");
		
		return "Service call";
	}
}

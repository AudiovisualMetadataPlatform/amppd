package edu.indiana.dlib.amppd.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.DashboardService;
import edu.indiana.dlib.amppd.service.impl.AmpUserServiceImpl;
import edu.indiana.dlib.amppd.web.DashboardResponse;
import edu.indiana.dlib.amppd.web.DashboardSearchQuery;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController

@Slf4j
public class DashboardController {
	
	@Autowired
	private DashboardService dashboardService;
	
	@PostMapping(path = "/dashboard", consumes = "application/json", produces = "application/json")
	public DashboardResponse getDashboardResults(@RequestBody DashboardSearchQuery query){
		log.info("Received request inside getDashboardResults");
		return dashboardService.getDashboardResults(query);
	}
	
	@PostMapping(path = "/dashboard/isfinal/{id}", consumes = "application/json", produces = "application/json")
	public boolean setIsFinal(@PathVariable("id") Long id, @RequestParam("isFinal") boolean isFinal){
		log.info("Setting file to final: " + id);
		return dashboardService.setResultIsFinal(id);
	}

	@PostMapping("/dashboard/refresh")
	public void refreshDashboardResults(){
		dashboardService.refreshAllDashboardResults();
	}

}

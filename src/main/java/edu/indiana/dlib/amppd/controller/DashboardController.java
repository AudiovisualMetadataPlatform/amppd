package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.DashboardResult;
import edu.indiana.dlib.amppd.service.DashboardService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class DashboardController {
	
	@Autowired
	private DashboardService dashboardService;
	
	@GetMapping("/workflow/dashboard")
	public List<DashboardResult> getDashboardResults(){
		return dashboardService.getDashboardResults();
	}

	@PostMapping("/workflow/dashboard/refresh")
	public void refreshDashboardResults(){
		dashboardService.refreshAllDashboardResults();
	}

}

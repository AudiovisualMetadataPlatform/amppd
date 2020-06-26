package edu.indiana.dlib.amppd.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
		
		
		
		if(query.getFilterByDates().size()>0)
		{
			log.info("the dates are:"+query.getFilterByDates().get(0)+" "+query.getFilterByDates().get(1));
			DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
			try {
			Date date = (Date)formatter.parse(query.getFilterByDates().get(0).toString());
			log.info(date+"");        

			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			String formatedDate = cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" +         cal.get(Calendar.YEAR);
			log.info("formatedDate : " + formatedDate); 
			}
			catch(Exception ex)
			{
				log.error(ex.getMessage());
			}
		}
		return dashboardService.getDashboardResults(query);
	}

	@PostMapping("/dashboard/refresh")
	public void refreshDashboardResults(){
		dashboardService.refreshAllDashboardResults();
	}

}

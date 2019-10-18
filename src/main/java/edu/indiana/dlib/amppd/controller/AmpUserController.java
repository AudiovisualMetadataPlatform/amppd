
  package edu.indiana.dlib.amppd.controller;
  
  import org.springframework.beans.factory.annotation.Autowired; 
  import org.springframework.ui.Model; 
  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.PostMapping;
  import org.springframework.web.bind.annotation.RequestParam;
  import org.springframework.web.bind.annotation.RestController;

  import edu.indiana.dlib.amppd.repository.AmpUserRepository;
  import lombok.extern.java.Log;
  
 /**
	 * Controller for REST operations on Login.
	 * 
	 * @author vinitab
	 *
	 */
  @RestController
  
  @Log 
  public class AmpUserController{
  
	private AmpUserRepository ampUser;
	
	
	  @GetMapping("/login") public String index() { return "index"; }
	  
	  @PostMapping("/login") String login(@RequestParam(value="name") String name)
	  { return "redirect:/home"; }
	  
	  @PostMapping("/register") String register(@RequestParam(value="name") String
	  name) { return "redirect:/home"; }
	  
	  @GetMapping("/welcome") public String welcome(Model model) { return "home"; }
	 
	 
  }
		 
package edu.indiana.dlib.amppd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.AmpUser;

@CrossOrigin(origins = "*")
public class AmpUserRepository {
	
	public interface UserRepository extends JpaRepository<AmpUser, Long> {
	    AmpUser findByUsername(String username) ;
	}
}

package edu.indiana.dlib.amppd.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.indiana.dlib.amppd.model.AmpUser;

public class AmpUserRepository {
	
	public interface UserRepository extends JpaRepository<AmpUser, Long> {
	    AmpUser findByUsername(String username);
	}
}

package edu.indiana.dlib.amppd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.AmpUser;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface AmpUserRepository extends CrudRepository<AmpUser, Long>{
	
	@Query(value = "select 1 from AmpUser i where i.username = :username and i.password = :pswd and i.approved=true")
	String findByApprovedUsername(@Param("username") String username, @Param("pswd") String pswd);
	
	@Query(value = "select case when COUNT(*)>0 then true else false end from AmpUser i where i.username = :username")
	boolean usernameExists(@Param("username") String username);

	Optional<AmpUser> findByUsername(String username);
	Optional<AmpUser> findByEmail(String email);			
	
	@Query(value = "update AmpUser set password = :pswd where username = :username and id = :id")
	String updatePassword(@Param("username") String username, @Param("pswd") String pswd, @Param("id") Long id);
}
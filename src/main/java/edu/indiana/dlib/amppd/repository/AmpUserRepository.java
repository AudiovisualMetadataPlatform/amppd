package edu.indiana.dlib.amppd.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.AmpUser;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface AmpUserRepository extends CrudRepository<AmpUser, Long>{
	
	@Query(value = "select 1 from AmpUser i where i.username = :username and i.password = :pswd")
	String findByUsername(@Param("username") String username, @Param("pswd") String pswd);
}

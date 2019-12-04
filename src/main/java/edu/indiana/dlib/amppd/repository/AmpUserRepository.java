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
	
	@Query(value = "select i.password from AmpUser i where i.username = :username ")
	String findByUsername(@Param("username") String username);
}
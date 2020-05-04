package edu.indiana.dlib.amppd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.AmpUser;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface AmpUserRepository extends PagingAndSortingRepository<AmpUser, Long>{
	
	@Query(value = "select 1 from AmpUser i where i.email = :email and i.password = :pswd and i.approve_status=:approve_status")
	String findByApprovedUser(@Param("email") String email, @Param("pswd") String pswd, @Param("approve_status") AmpUser.State approve_status);
	
	@Query(value = "select case when COUNT(*)>0 then true else false end from AmpUser i where i.username = :username")
	boolean usernameExists(@Param("username") String username);
	
	@Query(value = "select case when COUNT(*)>0 then true else false end from AmpUser i where i.email = :email")
	boolean emailExists(@Param("email") String email);

	Optional<AmpUser> findByUsername(String username);
	Optional<AmpUser> findByEmail(String email);			
	
	@Transactional
	@Modifying
	@Query(value = "update AmpUser set password = :pswd where username = :username and id = :id")
	int updatePassword(@Param("username") String username, @Param("pswd") String pswd, @Param("id") Long id);
	
	@Transactional
	@Modifying
	@Query(value = "update AmpUser set approve_status = :approve_status where id = :id")
	int updateApprove_status(@Param("id") Long id, @Param("approve_status") AmpUser.State approve_status);
}
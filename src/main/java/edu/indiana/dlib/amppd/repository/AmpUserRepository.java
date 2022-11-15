package edu.indiana.dlib.amppd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.AmpUser;


@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface AmpUserRepository extends PagingAndSortingRepository<AmpUser, Long>{
	
	AmpUser findFirstByUsername(String username);
	
	@Query(value = "select 1 from AmpUser i where i.username = :username and i.password = :pswd and i.status=:status")
	String findByApprovedUser(@Param("username") String username, @Param("pswd") String pswd, @Param("status") AmpUser.State status);
	
	@Query(value = "select case when COUNT(*)>0 then true else false end from AmpUser i where i.username = :username")
	boolean usernameExists(@Param("username") String username);
	
	@Query(value = "select case when COUNT(*)>0 then true else false end from AmpUser i where i.email = :email")
	boolean emailExists(@Param("email") String email);

	Optional<AmpUser> findByUsername(String username);
	Optional<AmpUser> findByEmail(String email);			
	
	@Modifying
	@Query(value = "update AmpUser set password = :pswd where username = :username and id = :id")
	int updatePassword(@Param("username") String username, @Param("pswd") String pswd, @Param("id") Long id);
	
	@Modifying
	@Query(value = "update AmpUser set status = :status where id = :id")
	int updateStatus(@Param("id") Long id, @Param("status") AmpUser.State status);
}
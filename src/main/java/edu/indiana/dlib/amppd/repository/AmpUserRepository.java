package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.AmpUser.Status;
import edu.indiana.dlib.amppd.model.dto.AmpUserDto;


@RepositoryRestResource(excerptProjection = AmpUserDto.class, collectionResourceRel = "users", path = "users")
public interface AmpUserRepository extends PagingAndSortingRepository<AmpUser, Long>{

	AmpUser findFirstByUsername(String username);
	Optional<AmpUser> findByUsername(String username);
	Optional<AmpUser> findByEmail(String email);			

	@Query(value = "select u from AmpUser u where u.status = :status and (" + 
			"lower(u.lastName) like lower(concat(:nameStarting,'%')) or " +
			"lower(u.firstName) like lower(concat(:nameStarting,'%')) or " +
			"lower(u.username) like lower(concat(:nameStarting,'%'))) " +
			"order by u.lastName, u.firstName, u.username")
	List<AmpUserDto> findByStatusAndNameStartsOrderByName(Status status, String nameStarting);
	List<AmpUserDto> findByStatusAndLastNameStartsWithIgnoreCaseOrderByUsername(Status status, String nameStarting);
	List<AmpUserDto> findByStatusAndFirstNameStartsWithIgnoreCaseOrderByUsername(Status status, String nameStarting);
	List<AmpUserDto> findByStatusAndUsernameStartsWithIgnoreCaseOrderByUsername(Status status, String nameStarting);

	@Query(value = "select u from AmpUser u where u.status = :status and u.id not in :idsExcluding and (" + 
			"lower(u.lastName) like lower(concat(:nameStarting,'%')) or " +
			"lower(u.firstName) like lower(concat(:nameStarting,'%')) or " +
			"lower(u.username) like lower(concat(:nameStarting,'%'))) " +
			"order by u.lastName, u.firstName, u.username")
	List<AmpUserDto> findByStatusAndNameStartsAndIdNotInOrderByName(Status status, String nameStarting, List<Long> idsExcluding);
	List<AmpUserDto> findByStatusAndLastNameStartsWithIgnoreCaseAndIdNotInOrderByUsername(Status status, String nameStarting, List<Long> idsExcluding);
	List<AmpUserDto> findByStatusAndFirstNameStartsWithIgnoreCaseAndIdNotInOrderByUsername(Status status, String nameStarting, List<Long> idsExcluding);
	List<AmpUserDto> findByStatusAndUsernameStartsWithIgnoreCaseAndIdNotInOrderByUsername(Status status, String nameStarting, List<Long> idsExcluding);

	@Query(value = "select 1 from AmpUser i where i.username = :username and i.password = :pswd and i.status=:status")
	String findByApprovedUser(@Param("username") String username, @Param("pswd") String pswd, @Param("status") AmpUser.Status status);

	@Query(value = "select case when COUNT(*)>0 then true else false end from AmpUser i where i.username = :username")
	boolean usernameExists(@Param("username") String username);

	@Query(value = "select case when COUNT(*)>0 then true else false end from AmpUser i where i.email = :email")
	boolean emailExists(@Param("email") String email);

	@Modifying
	@Query(value = "update AmpUser set password = :pswd where username = :username and id = :id")
	int updatePassword(@Param("username") String username, @Param("pswd") String pswd, @Param("id") Long id);

	@Modifying
	@Query(value = "update AmpUser set status = :status where id = :id")
	int updateStatus(@Param("id") Long id, @Param("status") AmpUser.Status status);

}
package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.AmpUser.Status;
import edu.indiana.dlib.amppd.model.projection.AmpUserBrief;


//@RepositoryRestResource(excerptProjection = AmpUserBrief.class, collectionResourceRel = "users", path = "users")
//@RepositoryRestResource(exported = false)
public interface AmpUserRepository extends PagingAndSortingRepository<AmpUser, Long>{

	AmpUser findFirstByUsername(String username);
	AmpUserBrief findFirstByUsernameAndPasswordAndStatus(String username, String password, Status status);

	Optional<AmpUser> findByUsername(String username);
	Optional<AmpUser> findByEmail(String email);			

	@Query(value = "select u from AmpUser u where u.status = :status and (" + 
			"lower(u.lastName) like lower(concat(:nameStarting,'%')) or " +
			"lower(u.firstName) like lower(concat(:nameStarting,'%')) or " +
			"lower(u.username) like lower(concat(:nameStarting,'%'))) " +
			"order by u.lastName, u.firstName, u.username")
	List<AmpUserBrief> findByStatusAndNameStartsOrderByName(Status status, String nameStarting);
	List<AmpUserBrief> findByStatusAndLastNameStartsWithIgnoreCaseOrderByUsername(Status status, String nameStarting);
	List<AmpUserBrief> findByStatusAndFirstNameStartsWithIgnoreCaseOrderByUsername(Status status, String nameStarting);
	List<AmpUserBrief> findByStatusAndUsernameStartsWithIgnoreCaseOrderByUsername(Status status, String nameStarting);

	@Query(value = "select u from AmpUser u where u.status = :status and u.id not in :idsExcluding and (" + 
			"lower(u.lastName) like lower(concat(:nameStarting,'%')) or " +
			"lower(u.firstName) like lower(concat(:nameStarting,'%')) or " +
			"lower(u.username) like lower(concat(:nameStarting,'%'))) " +
			"order by u.lastName, u.firstName, u.username")
	List<AmpUserBrief> findByStatusAndNameStartsAndIdNotInOrderByName(Status status, String nameStarting, List<Long> idsExcluding);
	List<AmpUserBrief> findByStatusAndLastNameStartsWithIgnoreCaseAndIdNotInOrderByUsername(Status status, String nameStarting, List<Long> idsExcluding);
	List<AmpUserBrief> findByStatusAndFirstNameStartsWithIgnoreCaseAndIdNotInOrderByUsername(Status status, String nameStarting, List<Long> idsExcluding);
	List<AmpUserBrief> findByStatusAndUsernameStartsWithIgnoreCaseAndIdNotInOrderByUsername(Status status, String nameStarting, List<Long> idsExcluding);
	
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
package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.TimedToken;

@RepositoryRestResource(exported = false)
public interface TimedTokenRepository extends CrudRepository<TimedToken, Long>{		
	Optional<TimedToken> findByToken(String token);		
	Optional<TimedToken> findByUser(AmpUser user);

	@Modifying
	@Query(value = "update TimedToken set token = :token, expiry_date= :expiration_date  where user_id = :id")
	int updateToken( @Param("token") String token, @Param("id") Long id, @Param("expiration_date") Date expiry_date);

	@Query(value = "select count(*) from TimedToken where user_id = :id")
	int ifExists(@Param("id") Long id);
}
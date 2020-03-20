package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Passwordresettoken;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "token", path = "token")
public interface PasswordTokenRepository extends CrudRepository<Passwordresettoken, Long>{
		
		Optional<Passwordresettoken> findByToken(String token);
		
		Optional<Passwordresettoken> findByUser(AmpUser user);
		
		@Transactional
		@Modifying
		@Query(value = "update Passwordresettoken set token = :token, expiry_date= :expiration_date  where user_id = :id")
		int updateToken( @Param("token") String token, @Param("id") Long id, @Param("expiration_date") Date expiry_date);
		
		@Query(value = "select count(*) from Passwordresettoken where user_id = :id")
		int ifExists(@Param("id") Long id);
}
package edu.indiana.dlib.amppd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.PasswordResetToken;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "token", path = "token")
public interface PasswordTokenRepository extends CrudRepository<PasswordResetToken, Long>{
		
		@Query(value = "select 1 from PasswordResetToken i where i.token = :token")
		String findByToken(@Param("token") Long token);
		
		Optional<PasswordResetToken> findByToken(String token);
}

package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.Supplement;

@NoRepositoryBean
public interface SupplementRepository<S extends Supplement> extends AssetRepository<S> {

	@Query(value = "select i from Supplement i where i.label = :label and primaryfile_id is not null")
	List<S> findByLabel(@Param("label") String label); 
}

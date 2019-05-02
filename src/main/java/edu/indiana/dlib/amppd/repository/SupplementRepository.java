package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.Supplement;

@NoRepositoryBean
public interface SupplementRepository<S extends Supplement> extends PagingAndSortingRepository<S, Long> {
	List<S> findByName(@Param("name") String name);

	List<S> findByOriginalFilename(@Param("originalFilename") String originalFilename);
	
}

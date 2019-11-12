package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.Asset;

@NoRepositoryBean
public interface AssetRepository<S extends Asset> extends ContentRepository<S> {
	
	List<S> findByOriginalFilename(@Param("originalFilename") String originalFilename);

	List<S> findByPathname(@Param("pathname") String pathname); // TODO: use customized impl to do match with SQL LIKE instead of =
	
}

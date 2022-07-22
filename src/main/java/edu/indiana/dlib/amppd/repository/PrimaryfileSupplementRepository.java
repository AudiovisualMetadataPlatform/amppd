package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.projection.PrimaryfileSupplementBrief;


@RepositoryRestResource(excerptProjection = PrimaryfileSupplementBrief.class)
public interface PrimaryfileSupplementRepository extends SupplementRepository<PrimaryfileSupplement> {

	List<PrimaryfileSupplement> findByPrimaryfileIdAndName(Long primaryfileId, String name);
	List<PrimaryfileSupplement> findByPrimaryfileIdAndCategory(Long primaryfileId, String category); 
	List<PrimaryfileSupplement> findByPrimaryfileIdAndCategoryAndOriginalFilenameEndsWith(Long primaryfileId, String category, String format); 
	List<PrimaryfileSupplement> findByPrimaryfileIdAndNameAndCategoryAndOriginalFilenameEndsWith(Long primaryfileId, String name, String category, String format); 
	
}

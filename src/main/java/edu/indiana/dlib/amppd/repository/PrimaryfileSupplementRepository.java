package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.projection.PrimaryfileSupplementBrief;


@RepositoryRestResource(excerptProjection = PrimaryfileSupplementBrief.class)
public interface PrimaryfileSupplementRepository extends SupplementRepository<PrimaryfileSupplement> {

	@RestResource(exported = false)
	int countByPrimaryfileItemCollectionUnitId(Long unitId);
	
	@RestResource(exported = false)
	int countByPrimaryfileItemCollectionId(Long collectionId);
	
	@RestResource(exported = false)
	int countByPrimaryfileItemId(Long itemId);
	
	@RestResource(exported = false)
	int countByPrimaryfileId(Long primaryfileId);
	
	@RestResource(exported = false)
	List<PrimaryfileSupplementBrief> findBy();
	
	@RestResource(exported = false)
	List<PrimaryfileSupplementBrief> findByPrimaryfileItemCollectionUnitIdIn(Set<Long> acUnitIds);

	@RestResource(exported = false)
	List<PrimaryfileSupplement> findByPrimaryfileId(Long primaryfileId);
	
	@RestResource(exported = false)
	List<PrimaryfileSupplement> findByPrimaryfileIdAndName(Long primaryfileId, String name);
	
	@RestResource(exported = false)
	List<PrimaryfileSupplement> findByPrimaryfileIdAndCategory(Long primaryfileId, String category); 
	
	@RestResource(exported = false)
	List<PrimaryfileSupplement> findByPrimaryfileIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long primaryfileId, String category, String format); 
	
	@RestResource(exported = false)	
	List<PrimaryfileSupplement> findByPrimaryfileIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long primaryfileId, String name, String category, String format); 
	
}

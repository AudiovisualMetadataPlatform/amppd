package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.UnitSupplement;
import edu.indiana.dlib.amppd.model.projection.UnitSupplementBrief;

@RepositoryRestResource(excerptProjection = UnitSupplementBrief.class)
public interface UnitSupplementRepository extends SupplementRepository<UnitSupplement> {

	@RestResource(exported = false)
	int countByUnitId(Long unitId);
	
	@RestResource(exported = false)
	List<UnitSupplementBrief> findBy();
	
	@RestResource(exported = false)
	List<UnitSupplementBrief> findByUnitIdIn(Set<Long> acUnitIds);
	
	@RestResource(exported = false)
	List<UnitSupplement> findByUnitId(Long unitId);
	
	@RestResource(exported = false)
	List<UnitSupplement> findByUnitIdAndName(Long unitId, String name);
	
	@RestResource(exported = false)
	List<UnitSupplement> findByUnitIdAndCategory(Long unitId, String category); 
	
	@RestResource(exported = false)
	List<UnitSupplement> findByUnitIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long unitId, String category, String format); 
	
	@RestResource(exported = false)
	List<UnitSupplement> findByUnitIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long unitId, String name, String category, String format); 

}

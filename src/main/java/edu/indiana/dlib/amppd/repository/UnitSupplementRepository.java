package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.UnitSupplement;
import edu.indiana.dlib.amppd.model.projection.UnitSupplementBrief;

@RepositoryRestResource(excerptProjection = UnitSupplementBrief.class)
public interface UnitSupplementRepository extends SupplementRepository<UnitSupplement> {

	List<UnitSupplementBrief> findBy();
	List<UnitSupplementBrief> findByUnitIdIn(Set<Long> acUnitIds);
	
	List<UnitSupplement> findByUnitId(Long unitId);
	List<UnitSupplement> findByUnitIdAndName(Long unitId, String name);
	List<UnitSupplement> findByUnitIdAndCategory(Long unitId, String category); 
	List<UnitSupplement> findByUnitIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long unitId, String category, String format); 
	List<UnitSupplement> findByUnitIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long unitId, String name, String category, String format); 

}

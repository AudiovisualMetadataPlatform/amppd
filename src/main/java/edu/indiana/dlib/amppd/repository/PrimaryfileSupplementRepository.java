package edu.indiana.dlib.amppd.repository;

import java.util.List;

import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;


//@RepositoryRestResource(collectionResourceRel = "primaryfileSupplements", path = "primaryfileSupplements")
public interface PrimaryfileSupplementRepository extends SupplementRepository<PrimaryfileSupplement> {

	List<PrimaryfileSupplement> findByPrimaryfileIdAndName(Long primaryfileId, String name);
	List<PrimaryfileSupplement> findByItemIdAndCategory(Long primaryfileId, String category); 
	List<PrimaryfileSupplement> findByItemIdAndCategoryAndOriginalFilenameLike(Long primaryfileId, String category, String fileExtension); 
	List<PrimaryfileSupplement> findByItemIdAndNameAndCategoryAndOriginalFilenameLike(Long primaryfileId, String name, String category, String fileExtension); 
	
}

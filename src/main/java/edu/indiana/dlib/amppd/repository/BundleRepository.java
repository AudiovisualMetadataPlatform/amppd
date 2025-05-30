package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import edu.indiana.dlib.amppd.model.Bundle;


//@RepositoryRestResource(collectionResourceRel = "bundles", path = "bundles")
public interface BundleRepository extends DataentityRepository<Bundle> {
		
	Bundle findFirstByName(String name);
	
	List<Bundle> findBy();
	List<Bundle> findByName(String name);
	List<Bundle> findByNameContainingIgnoreCase(String name);
	List<Bundle> findByCreatedBy(String createdBy);
	List<Bundle> findByNameContainingIgnoreCaseAndCreatedBy(String name, String createdBy);
	
	@Query(value = "select b from Bundle b where b.name is not null and b.name != '' and b.primaryfiles.size > 0 order by createdBy, name")
	List<Bundle> findAllWithNonEmptyNameNonEmptyPrimaryfiles();

	// delete primaryfile with the given primaryfileId from all bundles it's associated with
	@Modifying
	@Query(nativeQuery = true,
		value = "delete from bundle_primaryfile where primaryfile_id = :primaryfileId")
	void deletePrimaryfileFromBundles(Long primaryfileId);

	// delete bundle with the given bundleId from all primaryfiles it's associated with
	@Modifying
	@Query(nativeQuery = true,
		value = "delete from bundle_primaryfile where bundle_id = :bundleId")
	void deleteBundleFromPrimaryfiles(Long bundleId);

}

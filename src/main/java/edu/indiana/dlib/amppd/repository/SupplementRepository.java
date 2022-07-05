package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;

import edu.indiana.dlib.amppd.model.Supplement;

@NoRepositoryBean
public interface SupplementRepository<S extends Supplement> extends AssetRepository<S> {
	
	List<Supplement> findByCategory(String category); 
	List<Supplement> findByCategoryAndOriginalFilenameLike(String category, String fileExtension); 
	List<Supplement> findByNameAndCategoryAndOriginalFilenameLike(String name, String category, String fileExtension); 
	
	/* TODO 
	 *  The API for supplement creation can be disabled by setting @RepositoryRestResource export = false for saveOnCreation.
	 *  Currently the saveOnCreation API doesn't require media file when creating a supplement; 
	 *  rather, media ingestion can be done with the file upload API after the supplement is created;
	 *  this could break integrity of supplement data and workflow related operations.
	 *  To achieve both in one step, the saveOnCreation API might need customization (if possible with Spring Data Rest) 
	 *  to allow input Content-Type application/stream, because MultipartFile may not be serializable to JSON.
	 *  Alternatively, supplement creation/ingestion can be achieved in one step by the "/***s/{***Id}/addSupplement" API.
	 */ 

}

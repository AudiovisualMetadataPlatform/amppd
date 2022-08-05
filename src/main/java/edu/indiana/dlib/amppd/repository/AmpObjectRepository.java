package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.AmpObject;

@NoRepositoryBean
public interface AmpObjectRepository <S extends AmpObject> extends PagingAndSortingRepository<S, Long> {
	
	List<S> findByCreatedDate(@Param("createdDate") Date createdDate);
	List<S> findByCreatedBy(@Param("createdBy") String createdBy);
	List<S> findByModifiedDate(@Param("modifiedDate") String createdDate);
	List<S> findByModifiedBy(@Param("modifiedBy") String modifiedBy);

	// delete obsolete record
	List<S> deleteByModifiedDateBefore(Date dateObsolete);
	
}

package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.HmgmNote;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "hmgmNotes", path = "hmgmNotes")
public interface HmgmNoteRepository extends PagingAndSortingRepository<HmgmNote, Long> {
	
}

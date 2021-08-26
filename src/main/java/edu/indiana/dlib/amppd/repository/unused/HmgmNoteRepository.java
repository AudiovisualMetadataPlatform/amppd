package edu.indiana.dlib.amppd.repository.unused;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.unused.HmgmNote;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "hmgmNotes", path = "hmgmNotes")
public interface HmgmNoteRepository extends PagingAndSortingRepository<HmgmNote, Long> {
	
}

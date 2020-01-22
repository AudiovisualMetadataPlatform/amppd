package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.Hmgmtask;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "hmgmtasks", path = "hmgmtasks")
public interface HmgmtaskRepository extends DataentityRepository<Hmgmtask> {

}

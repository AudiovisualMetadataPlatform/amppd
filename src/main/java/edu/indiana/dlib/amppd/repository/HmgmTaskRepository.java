package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.HmgmTask;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "hmgmTasks", path = "hmgmTasks")
public interface HmgmTaskRepository extends DataentityRepository<HmgmTask> {

}

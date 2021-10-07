package edu.indiana.dlib.amppd.repository.unused;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.unused.HmgmTask;
import edu.indiana.dlib.amppd.repository.DataentityRepository;

//@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "hmgmTasks", path = "hmgmTasks")
public interface HmgmTaskRepository extends DataentityRepository<HmgmTask> {

}

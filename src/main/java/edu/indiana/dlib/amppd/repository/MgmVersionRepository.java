package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.format.annotation.DateTimeFormat;

import edu.indiana.dlib.amppd.model.MgmVersion;

public interface MgmVersionRepository extends AmpObjectRepository<MgmVersion> {
	
	// find all versions of the given MGM
	List<MgmVersion> findByMgmId(Long mgmId);
	
	// find the latest MGM version before the given date
	// this method is equivalent to findLatestByMgmId below
	MgmVersion findFirstByMgmIdAndUpgradeDateBeforeOrderByUpgradeDateDesc(Long mgmId, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") Date upgradeDate);

	// find the latest MGM version before the given date
	@Query(value = "select v from MgmVersion v where v.mgm.id = :mgmId and v.upgradeDate < :invocationTime order by upgradeDate desc")
	List<MgmVersion> findLatestByMgmId(Long mgmId, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") Date invocationTime);

}

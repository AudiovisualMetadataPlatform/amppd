package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import edu.indiana.dlib.amppd.model.MgmTool;


public interface MgmToolRepository extends AmpObjectRepository<MgmTool> {
	
	// find the MGM of the given toolId
	// since toolId is unique, it's safe to findFirstBy
	MgmTool findFirstByToolId(String toolId);

	// delete obsolete record
	List<MgmTool> deleteByModifiedDateBefore(Date dateObsolete);
	
}

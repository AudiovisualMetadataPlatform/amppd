package edu.indiana.dlib.amppd.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.repository.RoleRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.RoleService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RoleService.
 * @author yingfeng
 */
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UnitRepository unitRepository;	
	
	@Value("#{'${amppd.unitRoles}'.split(',')}")
	private List<String> unitRoleNames;
	

	/**
	 * @See edu.indiana.dlib.amppd.service.RoleService.getUnitRoleNames()
	 */
	@Override
	public List<String> getUnitRoleNames() {
		return unitRoleNames; 
	}
	
}

package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import lombok.Data;

/**
 * Organization unit that owns collections and workflows.
 * @author yingfeng
 *
 */
@Entity
@Data
public class Unit extends Content {
	
	@OneToMany(mappedBy="unit")
    private List<Collection> collections;

	@OneToMany(mappedBy="unit")
	private List<Workflow> workflows;
	
}

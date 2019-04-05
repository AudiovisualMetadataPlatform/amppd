package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

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
    private ArrayList<Collection> collections;

	@OneToMany(mappedBy="unit")
	private ArrayList<Workflow> workflows;
	
}

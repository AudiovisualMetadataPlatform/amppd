package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;

import lombok.Data;

/**
 * Supplement is a file (either media or annotation) used as supplemental material to assist metadata retrieval for a primary through a workflow.
 * @author yingfeng
 *
 */
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public class Supplement extends Asset {
	
	// TODO double check the relationship
	@ManyToMany
	private List<Bag> bags;
	
}

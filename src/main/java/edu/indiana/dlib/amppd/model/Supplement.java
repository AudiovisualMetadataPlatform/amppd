package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Supplement is a file (either media or annotation) used as supplemental material to assist metadata retrieval for a primaryfile through a workflow.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
public abstract class Supplement extends Asset {
	
	// TODO double check the relationship
//	@ManyToMany
//	private List<Bag> bags;
	
}

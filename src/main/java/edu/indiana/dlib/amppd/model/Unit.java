package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Organization unit that owns collections and workflows.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniqueUnitName", columnNames = {"name"})})
@UniqueName(message="unit name must be unique")
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class Unit extends Dataentity {

	@OneToMany(mappedBy="unit")
	@JsonBackReference(value="collections")
    private Set<Collection> collections;

	// TODO: Unit & Workflow do not have a 1:M ownership relation, but could have a M:M access relation. When we add access control we shall reconsider this mapping 
//	@OneToMany(mappedBy="unit")
//	private Set<Workflow> workflows;
	
}

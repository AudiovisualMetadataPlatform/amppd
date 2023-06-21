package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.validator.EnumConfig;
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
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Unit extends Dataentity {

	/* Note:
	 * Originally TaskManager was defined as enum type, for the sake of ensuring only a predefined set of options are allowed.
	 * However, enum might be serialized into integer values which need to be interpreted by external apps such as amppd-ui and HMGM tools, which would cause extra dependency. 
	 * It would be better to use a string representation and give the referring code flexibility on how to process (and validate) the values.
	 */
	@NotBlank
	@EnumConfig(property = "taskManagers")
	private String taskManager;
	
	@OneToMany(mappedBy="unit", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="collections")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<Collection> collections;

	@OneToMany(mappedBy="unit", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="supplements")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<UnitSupplement> supplements;
	
	@OneToMany(mappedBy="unit", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@JsonBackReference(value="roles")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<Role> roles;
	
	@OneToMany(mappedBy="unit", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@JsonBackReference(value="roleAssignments")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<RoleAssignment> roleAssignments;
	
//	@JsonIgnore
//    public Long getAcUnitId() {
//    	return getId();
//    }

	// TODO: Unit & Workflow do not have a 1:M ownership relation, but could have a M:M access relation. When we add access control we shall reconsider this mapping 
//	@OneToMany(mappedBy="unit")
//	private Set<Workflow> workflows;
	
}

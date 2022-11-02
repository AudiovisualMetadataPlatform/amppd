package edu.indiana.dlib.amppd.model;


import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Information about a role, either with global or unit scope, including the actions it can perform. 
 * @author yingfeng
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "entityType"),
		@Index(columnList = "name"),
		@Index(columnList = "entityType, name", unique = true),
		@Index(columnList = "unit_id"),
		@Index(columnList = "unit_id, name", unique = true),
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Role extends AmpObject {
    
	// the granularity of roles only go down to Colleciton level. AMP refers to global roles.
	public enum EntitytType { AMP, Unit, Collection }
	
	// the type of entity this role is associated with
	@NotBlank
	@Enumerated(EnumType.STRING)
    private EntitytType entityType;
    
	@NotBlank
    @Type(type="text")
    private String name;
    
    @Type(type="text")
    private String description;

    // the unit within which scope this role is visible/applicable;
    // if null, it's a global role with static permission settings populated from configuration;
    // otherwise, it's a dynamic role with permissions dynamically set by its unit admin for that unit
	@ManyToOne
    private Unit unit;
    
	// permissions: the actions this role can perform 
	@ManyToMany
    @JoinTable(name = "role_action", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "action_id"))
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Set<Action> actions;
	
	// role-entity-user assignment
	@OneToMany(mappedBy="role", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="roleAssignements")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Set<RoleAssignment> roleAssignements;
	
	
	/**
	 * Return the entity type plus name as the complete role name to be displayed
	 * @return
	 */
	public String getCompleteName() {
		return entityType + " " + name;		
	}
	
}

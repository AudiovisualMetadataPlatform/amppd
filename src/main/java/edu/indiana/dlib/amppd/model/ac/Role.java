package edu.indiana.dlib.amppd.model.ac;


import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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

import edu.indiana.dlib.amppd.model.AmpObject;
import edu.indiana.dlib.amppd.model.Unit;
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
		@Index(columnList = "name"),
		@Index(columnList = "level", unique = true),
		@Index(columnList = "unit_id"),
		@Index(columnList = "unit_id, name", unique = true),
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Role extends AmpObject {

	@NotBlank
    @Type(type="text")
    private String name;
    
    @Type(type="text")
    private String description;

    @NotBlank
    // inheritance hierarchy level
    /* Note:
     * This is only used during role/permission refresh, to avoid manual input of redundant rows in permission table; it's not used for permission checking. 
     * The role hierarchy is linear and role level is unique, starting at 0 for the root (AMP Admin), and increasing by 1 with each lower level role.
     */
    private Integer level;	
    
    // the unit within which scope this role is visible/applicable;
    // if null, it's a global role with the same set of permissions shared across units;
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
	
}

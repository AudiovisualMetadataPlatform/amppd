package edu.indiana.dlib.amppd.model.ac;


import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
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
		@Index(columnList = "level"),
		@Index(columnList = "unit_id"),
		@Index(columnList = "unit_id, name", unique = true),
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Role extends AmpObject {

	public static final Integer MAX_LEVEL = Integer.MAX_VALUE;
	public static final String AMP_ADMIN_ROLE_NAME = "AMP Admin";
		
	@NotBlank
    @Type(type="text")
    private String name;
    
    @Type(type="text")
    private String description;

    @NotBlank
    // role assignment order 
    /* Note:
     * The role assignment order is linear and role levels below assignment threshold is unique.
     * It starts at 0 for the first role (AMP Admin) in the assignment order, and increases by 1 with each next role in the order.
     * Roles that can't participate in role assignment all have the same maximum level, which ensures that they can't assign any roles.
     * Unit scope roles are assigned with MAX_VALUE by default, as we disallow these roles to assign other roles, so they don't have a linear order.
     * For now, role level is also used as inheritance hierarchy level during role/action/permission initialization, to avoid manual input of redundant rows in permission table;
     * this usage, however, may not apply to the general cases when permissions can be configured by admin, at which point, AC table initialization shall be disabled.
     */
    private Integer level = MAX_LEVEL;
    
    // the unit within which scope this role is visible/applicable;
    // if null, it's a global role with the same set of permissions shared across units;
    // otherwise, it's a dynamic role with permissions dynamically set by its unit admin for that unit
	@ManyToOne
    private Unit unit;
    
	// permissions: the actions this role can perform 
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_action", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "action_id"))
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Set<Action> actions;
	
	// role-entity-user assignment
	@OneToMany(mappedBy="role", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@JsonBackReference(value="roleAssignements")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Set<RoleAssignment> roleAssignements;
	
}

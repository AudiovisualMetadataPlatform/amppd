package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Assignment of a user to a global role or a role associated with a particular unit or collection.
 * @author yingfeng
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "actionType", unique = true),
		@Index(columnList = "targetType", unique = true),
		@Index(columnList = "urlPattern", unique = true),
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class RoleAssignment extends AmpObject {

	@ManyToOne
    private AmpUser user;
	
	@ManyToOne
    private Role role;
	
	// ID of the host entity for the role
	private Long entityId;
	
//	@ManyToOne
	// reference to the role's host entity, which can be either a unit or collection or null;
	// it can't be directly mapped as an abstract superclass instance, but can be retrieved from repository as needed 
	@Transient
    private Dataentity entity;
		
}

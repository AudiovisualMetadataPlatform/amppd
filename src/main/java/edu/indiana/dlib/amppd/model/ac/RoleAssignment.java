package edu.indiana.dlib.amppd.model.ac;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import edu.indiana.dlib.amppd.model.AmpObject;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Assignment of a user to a global role or a role associated with a particular unit or collection.
 * @author yingfeng
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "user_id"),
		@Index(columnList = "role_id"),
		@Index(columnList = "unit_id"),
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@NoArgsConstructor 
@AllArgsConstructor 
public class RoleAssignment extends AmpObject {

	@ManyToOne
    private AmpUser user;
	
	@ManyToOne
    private Role role;
	
	/* Note:
	 * We will assign roles at AMP or Unit level, not any lower level, 
	 * thus the only entity type that can be associated with a role is Unit.
	 */
	// reference to the role's assignment unit
	@ManyToOne
    private Unit unit;
		
}

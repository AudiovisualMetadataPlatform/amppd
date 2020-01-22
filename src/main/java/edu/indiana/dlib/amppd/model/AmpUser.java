package edu.indiana.dlib.amppd.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.ToString;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@EntityListeners(AuditingEntityListener.class)
@Entity
@EqualsAndHashCode
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class AmpUser {
	
	/* Note:
	 * In the future when we implement full-featured access control, we shall define separate classes for Role and Permission, 
	 * and use User-Role-Permission relations to control access. 
	 * For now we use a simplified strategy with a flat role definition, i.e. each user has a static Role associated with specific permissions.
	 * Supervisor and Student are for Human MGM use only, and we assume they don't overlap with AMPPD Content User such as Collection Manager.
	 */	
    public enum Role {ADMIN, MANAGER, SUPERVISOR, STUDENT}
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
	private String username;
	private String email;	
	private String password;
	private Role role;
	
	// TODO change to Enum state (RQUESTED, APPROVED, ACTIVATED,  REJECTED) 
	private Boolean approved = false;	
	
	@OneToMany(mappedBy="assignedTo")
    private Set<HmgmTask> hmgmTasks;
	
}
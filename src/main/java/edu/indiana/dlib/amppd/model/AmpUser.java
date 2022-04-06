package edu.indiana.dlib.amppd.model;


import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Data
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
    public enum State {REQUESTED, ACCEPTED, ACTIVATED, REJECTED}
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
	
	//@NotNull
	@Index(unique="true")
	private String username;
	
	//@NotNull
	@Index(unique="true")
	private String email;	// email is always the same as username
	
	//@NotNull
	private String password;

	private String firstName;
	private String lastName;
		
	//@NotNull
	@Index
	@Enumerated(EnumType.STRING)
	private State status = State.REQUESTED;
	
	private Role role;	// currently not used

}
package edu.indiana.dlib.amppd.model;


import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "username", unique = true),
		@Index(columnList = "email", unique = true),
		@Index(columnList = "firstName"),
		@Index(columnList = "lastName"),
		@Index(columnList = "status"),	
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class AmpUser extends AmpObject {
	
	/* Note:
	 * In the future when we implement full-featured access control, we shall define separate classes for Role and Permission, 
	 * and use User-Role-Permission relations to control access. 
	 * For now we use a simplified strategy with a flat role definition, i.e. each user has a static Role associated with specific permissions.
	 * Supervisor and Student are for Human MGM use only, and we assume they don't overlap with AMPPD Content User such as Collection Manager.
	 */	
    public enum Role {ADMIN, MANAGER, SUPERVISOR, STUDENT}
    public enum Status {REQUESTED, ACCEPTED, ACTIVATED, REJECTED}
	
	@NotNull
	private String username;
	
	@NotNull
	private String email;	// email is always the same as username
	
	@NotNull
	private String password;

	@NotNull
	private String firstName;

	@NotNull
	private String lastName;
		
	@NotNull
	@Enumerated(EnumType.STRING)
	private Status status = Status.REQUESTED;
	
	@OneToMany(mappedBy="user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@JsonBackReference(value="roleAssignements")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Set<RoleAssignment> roleAssignements;
	
	// TODO replace with AMP Role
	private Role role;
		
	/**
	 * Check if the user is active.
	 * @return true if the user status is ACTIVATED, false otherwise.
	 */
	public boolean isActive() {
		return Status.ACTIVATED.equals(status);		
	}

}
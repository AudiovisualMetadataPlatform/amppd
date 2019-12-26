package edu.indiana.dlib.amppd.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.ToString;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@EntityListeners(AuditingEntityListener.class)
@Entity
@EqualsAndHashCode
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class User {
	
    public enum Role {USER, ADMIN, USER_MANAGER}
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
	private String username;
	private String email;	
	private String password;
	private Role role;
	
	// TODO change to Enum state (RQUESTED, APPROVED, ACTIVATED,  REJECTED) 
	private Boolean approved = false;	
}
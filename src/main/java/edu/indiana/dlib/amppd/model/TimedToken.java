package edu.indiana.dlib.amppd.model;


import java.util.Date;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * PasswordRestToken generates token that can be used for resetting the login password.
 * @author vinitab
 *
 */

@Data
@Entity
public class TimedToken {  
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
  
	@NotNull
	@Index(unique="true")
    @OneToOne(targetEntity = AmpUser.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private AmpUser user;
  
	@NotNull
	@Index(unique="true")
    private String token;  

	private Date expiryDate;
}    
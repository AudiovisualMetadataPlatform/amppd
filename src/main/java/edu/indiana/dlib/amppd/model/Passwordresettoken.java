package edu.indiana.dlib.amppd.model;


import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue; 
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.Data;

/**
 * PasswordRestToken generates token that can be used for resetting the login password.
 * @author vinitab
 *
 */

@Data
@Entity
public class Passwordresettoken {
  
    public static final int EXPIRATION = 60 * 5;
  
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
  
    private String token;
  
    @OneToOne(targetEntity = AmpUser.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "id")
    private AmpUser user;
  
    private Date expiryDate;
}
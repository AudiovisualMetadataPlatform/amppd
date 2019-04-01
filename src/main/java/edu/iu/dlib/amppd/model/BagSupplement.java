package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BagSupplement represents the M:M relationship between bags and supplements. 
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class BagSupplement {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;        
    private Long bagId;
    private Long supplementId;
       
}

// TODO this class may not be needed
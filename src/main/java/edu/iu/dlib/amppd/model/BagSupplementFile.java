package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * BagSupplementFile represents the M:M relationship between bags and supplement files. 
 * @author yingfeng
 *
 */
@Entity
public class BagSupplementFile {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;        
    private Long bagId;
    private Long supplementFileId;
       
}

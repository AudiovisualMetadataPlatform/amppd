package edu.iu.dlib.amppd.model;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;

/**
 * Super class for all data entities created in AMP. 
 * @author yingfeng
 *
 */
@MappedSuperclass
@Data
public abstract class AmpData {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private String createdBy;
    private Date dateCreated;

}

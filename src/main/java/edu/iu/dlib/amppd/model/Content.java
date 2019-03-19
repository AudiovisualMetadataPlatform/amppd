package edu.iu.dlib.amppd.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Super class for all data content entities.
 * @author yingfeng
 *
 */
@Entity
public abstract class Content {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private String createdBy;
    private Date dateCreated;
    
}

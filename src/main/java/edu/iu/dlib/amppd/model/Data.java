package edu.iu.dlib.amppd.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Super class for all data entities created in AMP. 
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public abstract class Data {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private String createdBy;
    private Date dateCreated;

}

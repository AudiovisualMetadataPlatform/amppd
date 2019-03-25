package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


/**
 * GroupBag represents the M:M relationship between groups and bags.
 * @author yingfeng
 *
 */
@Entity
public class GroupBag {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;    
    private Long groupId;
    private Long bagId;

}


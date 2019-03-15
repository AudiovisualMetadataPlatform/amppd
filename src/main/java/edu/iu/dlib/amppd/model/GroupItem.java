package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class GroupItem {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;    
    private Long groupId;
    private Long itemId;

}


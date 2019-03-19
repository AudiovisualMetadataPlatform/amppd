package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * ItemMasterFile represents the M:M relationship between items and master files. 
 * @author yingfeng
 *
 */
@Entity
public class ItemMasterFile {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;        
    private Long itemId;
    private Long masterFileId;
       
}

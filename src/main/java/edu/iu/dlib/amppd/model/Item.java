package edu.iu.dlib.amppd.model;


import java.util.ArrayList;

import javax.persistence.Entity;

import lombok.Data;

/**
 * Item represents an intellectual object that contains one or more masterfiles and none or multiple supplement files.
 * @author yingfeng
 *
 */
@Entity
@Data
public class Item extends Content {

    private Long collectionId;
    private Collection collection;	
    private ArrayList<Masterfile> masterfiles;
    private ArrayList<SupplementOfItem> supplements;
        
}

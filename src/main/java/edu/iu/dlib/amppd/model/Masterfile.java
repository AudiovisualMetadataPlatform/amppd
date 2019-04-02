package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

import lombok.Data;

/**
 * Masterfile is a file containing actual media content of any MIME type. A masterfile always associates with one and only one item.
 * @author yingfeng
 *
 */
@Entity
@Data
public class Masterfile extends Asset {

    private Long itemId;
    private Item item;
    private ArrayList<SupplementOfMasterfile> supplements;
    
}

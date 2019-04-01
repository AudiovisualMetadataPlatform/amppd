package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Masterfile is a file containing actual media content of any MIME type. A masterfile always associates with one and only one item.
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class Masterfile extends Asset {

    private Long itemId;
    private Item item;
    private ArrayList<SupplementOfMasterfile> supplements;
    
}

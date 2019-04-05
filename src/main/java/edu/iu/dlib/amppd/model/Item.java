package edu.iu.dlib.amppd.model;


import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

/**
 * Item represents an intellectual object that contains one or more primaries and none or multiple supplement files.
 * @author yingfeng
 *
 */
@Entity
@Data
public class Item extends Content {

	@OneToMany(mappedBy="item")
    private ArrayList<Primary> primaries;
    
	@OneToMany(mappedBy="item")
    private ArrayList<SupplementOfItem> supplements;

//	  private Long collectionId;
	@ManyToOne
	private Collection collection;	
		
}

package edu.indiana.dlib.amppd.model;


import java.util.List;

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
    private List<Primary> primaries;
    
	@OneToMany(mappedBy="item")
    private List<SupplementOfItem> supplements;

//	  private Long collectionId;
	@ManyToOne
	private Collection collection;	
		
}

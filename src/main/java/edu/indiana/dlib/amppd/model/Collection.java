package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

/**
 * Collection contains one or more items, and belongs to one and only one unit.
 * @author yingfeng
 *
 */
@Entity
@Data
public class Collection extends Content {

	@OneToMany(mappedBy="collection")
    private List<Item> items; 
	
	@OneToMany(mappedBy="collection")
    private List<SupplementOfCollection> supplements;
	
//	private Long unitId;
	@ManyToOne
	private Unit unit;
	
}

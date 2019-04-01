package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Collection contains one or more items, and belongs to one and only one unit.
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class Collection extends Content {

	private Long unitId;
	private Unit unit;
    private ArrayList<Item> items; 
    private ArrayList<SupplementOfCollection> supplements;
}

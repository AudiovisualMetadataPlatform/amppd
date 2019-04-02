package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

import lombok.Data;

/**
 * Collection contains one or more items, and belongs to one and only one unit.
 * @author yingfeng
 *
 */
@Entity
@Data
public class Collection extends Content {

	private Long unitId;
	private Unit unit;
    private ArrayList<Item> items; 
    private ArrayList<SupplementOfCollection> supplements;
}

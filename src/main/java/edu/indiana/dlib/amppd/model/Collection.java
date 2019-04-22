package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Collection contains one or more items, and belongs to one and only one unit.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class Collection extends Content {

	@OneToMany(mappedBy="collection")
    private List<Item> items; 
	
	@OneToMany(mappedBy="collection")
    private List<CollectionSupplement> supplements;
	
	@ManyToOne
	private Unit unit;
	
}

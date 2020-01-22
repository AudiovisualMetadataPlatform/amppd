package edu.indiana.dlib.amppd.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Collection contains one or more items, and belongs to one and only one unit.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class Collection extends Content {

	@OneToMany(mappedBy="collection")
    private Set<Item> items; 
	
	@OneToMany(mappedBy="collection")
    private Set<CollectionSupplement> supplements;
	
	@ManyToOne
	private Unit unit;
	
	public void addItem(Item item) {
		if(items==null) items = new HashSet<Item>();
		items.add(item);
	}
	
}

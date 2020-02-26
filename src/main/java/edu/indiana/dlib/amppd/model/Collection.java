package edu.indiana.dlib.amppd.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
    
	// the set of task management platforms AMPPD currently supports
	public enum TaskPlatform {JIRA, OPENPROJECT, REDMINE}
	
	// task platform can be chosen upon collection creation (or edit) by collection managers
    private TaskPlatform taskPlatform;
    
	@JsonBackReference(value="item")
	@OneToMany(mappedBy="collection")
    private Set<Item> items; 
	
	@OneToMany(mappedBy="collection")
    private Set<CollectionSupplement> supplements;

	@JsonBackReference(value="unit")
	@ManyToOne
	private Unit unit;
	
	public void addItem(Item item) {
		if(items==null) items = new HashSet<Item>();
		items.add(item);
	}
	
}

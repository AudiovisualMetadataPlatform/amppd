package edu.indiana.dlib.amppd.model;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

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
    
//	// the set of task management platforms AMPPD currently supports
//	public enum TaskManager {JIRA, OPENPROJECT, REDMINE}
//	
//	// task platform can be chosen upon collection creation (or edit) by collection managers
//    private TaskManager taskManager;
    
	/* Note:
	 * Originally TaskManager was defined as enum type, for the sake of ensuring only a predefined set of options are allowed.
	 * However, enum might be serialized into integer values which need to be interpretated by external apps such as amppd-ui and HMGM tools, which would cause extra dependency. 
	 * It would be better to use a string representation and give the referring code flexibility on how to process (and validate) the values.
	 */
	private String taskManager;
	
	@OneToMany(mappedBy="collection")
	@JsonBackReference(value="items")
    private Set<Item> items; 
	
	@OneToMany(mappedBy="collection")
	@JsonBackReference(value="supplements")
    private Set<CollectionSupplement> supplements;

	//@NotNull
	@Index
	@ManyToOne
	private Unit unit;
	
	public void addItem(Item item) {
		if(items==null) items = new HashSet<Item>();
		items.add(item);
	}
	
}

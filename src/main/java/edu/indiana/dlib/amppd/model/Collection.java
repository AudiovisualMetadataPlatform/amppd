package edu.indiana.dlib.amppd.model;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Index;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import edu.indiana.dlib.amppd.validator.EnumConfig;
import edu.indiana.dlib.amppd.validator.UniqueName;
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
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniqueCollectionNamePerUnit", columnNames = { "unit_id", "name" })})
@UniqueName(message="collection name must be unique within its parent unit")
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Collection extends Content {
    
	@NotNull
	@Index
	private Boolean active = true;	// newly created collection is active by default
	
	/* Note:
	 * Originally TaskManager was defined as enum type, for the sake of ensuring only a predefined set of options are allowed.
	 * However, enum might be serialized into integer values which need to be interpreted by external apps such as amppd-ui and HMGM tools, which would cause extra dependency. 
	 * It would be better to use a string representation and give the referring code flexibility on how to process (and validate) the values.
	 */
	@NotBlank
	@EnumConfig(property = "taskManagers")
	private String taskManager;
	
	@OneToMany(mappedBy="collection", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="items")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<Item> items; 
	
	@OneToMany(mappedBy="collection", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="supplements")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<CollectionSupplement> supplements;

	@NotNull
	@Index
	@ManyToOne
	private Unit unit;
	
	public void addItem(Item item) {
		if(items==null) items = new HashSet<Item>();
		items.add(item);
	}
	
}

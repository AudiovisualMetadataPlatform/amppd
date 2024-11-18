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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Formula;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
	
	// true if there aren't any incomplete workflow invocation on it
    @Formula("not exists (select w.id from workflow_result w where w.collection_id = id and w.status in ('SCHEDULED', 'IN_PROGRESS'))")
    private Boolean deletable;     
	
	public void addItem(Item item) {
		if(items==null) items = new HashSet<Item>();
		items.add(item);
	}
	
	@JsonIgnore
    public Long getAcUnitId() {
    	return unit.getAcUnitId();
    }


}

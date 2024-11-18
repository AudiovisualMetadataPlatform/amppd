package edu.indiana.dlib.amppd.model;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Formula;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * ItemSupplement is a supplemental file associated with an item and shared by all primaryfiles within that item.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniqueItemSupplementNamePerItem", columnNames = {"item_id", "name"})})
@UniqueName(message="Item supplement name must be unique within its parent item")
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class ItemSupplement extends Supplement {

	@NotNull
	@Index
	@ManyToOne
    private Item item;
	
	// true if there aren't any incomplete workflow invocation on its parent
    @Formula("not exists (select w.id from workflow_result w where w.item_id = item_id and w.status in ('SCHEDULED', 'IN_PROGRESS'))")
    private Boolean deletable;     	
	
	// it is never a groundtruth to be involved in MGM evaluation
    @Formula("false")
    private Boolean evaluated; 
    
	@JsonIgnore
    public Long getAcUnitId() {
    	return item.getAcUnitId();
    }

}

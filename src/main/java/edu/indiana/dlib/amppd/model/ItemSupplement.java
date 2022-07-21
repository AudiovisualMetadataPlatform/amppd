package edu.indiana.dlib.amppd.model;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniquePrimaryfileNamePerItem", columnNames = {"item_id", "name"})})
@UniqueName(message="itemSupplement name must be unique within its parent item")
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class ItemSupplement extends Supplement {

	@NotNull
	@Index
	@ManyToOne
    private Item item;
    
}

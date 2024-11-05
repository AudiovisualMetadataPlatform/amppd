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
 * CollectionSupplement is a supplemental file associated with a collection and shared by all items within that collection.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniqueCollectionSupplementNamePerCollection", columnNames = {"collection_id", "name"})})
@UniqueName(message="Collection supplement name must be unique within its parent collection")
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class CollectionSupplement extends Supplement {

	@NotNull
	@Index
	@ManyToOne
    private Collection collection;
	
	// true if there aren't any incomplete workflow invocation on its parent
    @Formula("not exists (select w.id from workflow_result w where w.collection_id = collection_id and w.status in ('SCHEDULED', 'IN_PROGRESS', 'PAUSED'))")
    private Boolean deletable;     	
	
	// it is never a groundtruth to be involved in MGM evaluation
    @Formula("false")
    private Boolean evaluated; 

    @JsonIgnore
    public Long getAcUnitId() {
    	return collection.getAcUnitId();
    }

}

package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * CollectionSupplement is a supplemental file associated with a collection and shared by all items within that collection.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class CollectionSupplement extends Supplement {

	@ManyToOne
    private Collection collection;
    
}

package edu.indiana.dlib.amppd.model;


import java.util.Set;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Item represents an intellectual object that contains one or more primaryfiles and none or multiple supplement files.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniqueItemNamePerCollection", columnNames = {"collection_id", "name"})})
@UniqueName(message="item name must be unique within its parent collection")
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class Item extends Content {
    
	@OneToMany(mappedBy="item")
	@JsonBackReference(value="primaryfiles")
    private Set<Primaryfile> primaryfiles;

	@OneToMany(mappedBy="item")
	@JsonBackReference(value="supplements")
    private Set<ItemSupplement> supplements;

	@NotNull
	@Index
	@ManyToOne
	private Collection collection;	
    
//    @ManyToMany
//    @JsonBackReference
//    private Set<Bundle> bundles;      
    	
}

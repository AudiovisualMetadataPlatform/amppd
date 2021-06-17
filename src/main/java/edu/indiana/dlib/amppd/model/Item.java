package edu.indiana.dlib.amppd.model;


import java.util.Set;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
@Transactional(readOnly=true, noRollbackFor=Exception.class)
public class Item extends Content {

    @Type(type="text")
    private String externalSource;	// external source/target system

    @Type(type="text")
    private String externalId;		// ID in the external system
    
	@OneToMany(mappedBy="item")
	@JsonBackReference(value="primaryfiles")
    private Set<Primaryfile> primaryfiles;

	@OneToMany(mappedBy="item")
	@JsonBackReference(value="supplements")
    private Set<ItemSupplement> supplements;

	//@NotNull
	@Index
	@ManyToOne
	private Collection collection;	
    
//    @ManyToMany
//    @JsonBackReference
//    private Set<Bundle> bundles;      
    	
}

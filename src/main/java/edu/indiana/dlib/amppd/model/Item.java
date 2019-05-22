package edu.indiana.dlib.amppd.model;


import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
// Avoid using @Data here as Lombok's impl of toString, equals, and hashCode doesn't handle circular references as in Bundle and Item and will cause StackOverflow exception.
public class Item extends Content {
    
	@OneToMany(mappedBy="item")
    private Set<Primaryfile> primaryfiles;

	@OneToMany(mappedBy="item")
    private Set<ItemSupplement> supplements;

	@ManyToOne
	private Collection collection;	
		
    @ManyToMany
    @JsonBackReference
    private Set<Bundle> bundles;      
    	
}

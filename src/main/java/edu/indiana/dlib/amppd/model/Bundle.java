package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Bundle is a container of one or multiple bags to which similar workflows can be applied.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
// Avoid using @Data here as Lombok's impl of toString, equals, and hashCode doesn't handle circular references as in Bundle and Item and will cause StackOverflow exception.
public class Bundle extends Dataentity {

	@ManyToMany(mappedBy = "bundles")
	// TODO following annotation was added to resolve a recursive reference issue due to M:M relationship with Item, but it causes exceptions in repository requests   
//	@JsonManagedReference	
    private Set<Item> items;
    
//    @ManyToMany(mappedBy = "bundles")
//    private Set<Bag> bags;

}
  


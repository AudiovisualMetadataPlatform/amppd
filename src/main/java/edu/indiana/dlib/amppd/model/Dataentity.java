package edu.indiana.dlib.amppd.model;

import javax.jdo.annotations.Index;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Base class for most media data entities created in AMP. 
 * @author yingfeng
 *
 */
//@UniqueName
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public abstract class Dataentity extends AmpObject {
	
	@NotBlank
	@Index
    @Type(type="text")
    private String name;
    
    @Type(type="text")
    private String description;

    // TODO:
    // Uncomment @NotNull in all model classes after we fix unit tests to populate all non-null fields when saving to DB.
    
    // TODO: research LomBok issue: whenever no arg constructor exists (whether defined by code or by Lombok annotation) other constructors won't be added by Lombok despite the annotation
//    public Dataentity() {
//    	super();
//    }
//    
//    public Dataentity(String name, String description) {
//    	this.name = name;
//    	this.description = description;
//    }

//  abstract public Long getUnitId();
    

}

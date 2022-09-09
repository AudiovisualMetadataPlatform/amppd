package edu.indiana.dlib.amppd.model;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Base class for all MGM metadata related classes. 
 * @author yingfeng
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public abstract class MgmMeta extends AmpObject {

    @NotBlank
    private String name;	

    @NotBlank
    @Type(type="text")
    private String description;	

}

package edu.indiana.dlib.amppd.model;

import javax.jdo.annotations.Index;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;

import edu.indiana.dlib.amppd.validator.EnumConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Super class for all content related entities. It provides a container at various levels for content materials.
 * @author yingfeng
 *
 */
@MappedSuperclass
@Index(members={"externalSource","externalId"}, unique="true")
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public abstract class Content extends Dataentity {
	// TODO at the moment we do not require externalSource/Id on every collection/item, but this could change later
	
	@EnumConfig(property = "externalSources")
    private String externalSource;	// external source/target system

    @Type(type="text")
    private String externalId;		// ID in the external system    	
    
}

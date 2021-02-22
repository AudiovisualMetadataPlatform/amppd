package edu.indiana.dlib.amppd.model;

import javax.jdo.annotations.Index;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;

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

    //@Type(type="text")
    //private String externalSource;

    //@Type(type="text")
    //private String externalId;
    
}

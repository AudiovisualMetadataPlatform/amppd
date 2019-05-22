package edu.indiana.dlib.amppd.model;

import java.util.HashMap;

import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Super class for all content related entities. It provides a container at various levels for content materials.
 * @author yingfeng
 *
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public abstract class Content extends Dataentity {

    private HashMap<String, String> externalIds;

}

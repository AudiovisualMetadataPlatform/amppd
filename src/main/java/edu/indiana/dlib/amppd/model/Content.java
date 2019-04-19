package edu.indiana.dlib.amppd.model;

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import lombok.Data;

/**
 * Super class for all content related entities. It provides a container at various levels for content materials.
 * @author yingfeng
 *
 */
@MappedSuperclass
@Data
public abstract class Content extends Dataentity {

    private HashMap<String, String> externalIds;

}

package edu.iu.dlib.amppd.model;

import java.util.HashMap;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Super class for all content related entities. It provides a container at various levels for content materials.
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public abstract class Content extends Data {

    private HashMap<String, String> externalIds;

}

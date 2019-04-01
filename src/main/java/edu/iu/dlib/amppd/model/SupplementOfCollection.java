package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SupplementOfCollection is a supplemental file associated with a collection and shared by all items within that collection.
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class SupplementOfCollection extends Supplement {

    private Long collectionId;
    private Collection collection;
    
}

package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import lombok.Data;

/**
 * Group is a container of one or multiple bags to which similar workflows can be applied.
 * @author yingfeng
 *
 */
@Entity
@Data
public class Group extends AmpData {

    @ManyToMany(mappedBy = "groups")
    private List<Bag> bags;
    
}



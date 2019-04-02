package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

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
public class Group extends BO {

    @ManyToMany(mappedBy = "groups")
    private ArrayList<Bag> bags;
    
}



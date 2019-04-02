package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

import lombok.Data;

/**
 * Organization unit that owns collections and workflows.
 * @author yingfeng
 *
 */
@Entity
@Data
public class Unit extends Content {
	
    private ArrayList<Collection> collections;
    private ArrayList<Workflow> workflows;
}

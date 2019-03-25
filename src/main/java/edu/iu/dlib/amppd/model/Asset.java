package edu.iu.dlib.amppd.model;

import java.net.URI;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Asset represents a file containing either media content of any MIME type or annotation of a media file in text/json format, which can be the input/ouput of a workflow or MGM. 
 * @author yingfeng
 *
 */
@Entity
public class Asset {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
   
    private URI uri;
	HashMap<String, String> externalIds;
}

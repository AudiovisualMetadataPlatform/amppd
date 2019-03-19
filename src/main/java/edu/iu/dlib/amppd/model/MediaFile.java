package edu.iu.dlib.amppd.model;

import java.net.URI;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * MediaFile represents a file containing actual media content of any MIME type or annotation of some media file in text/json format, 
 * which can be the input/ouput of a workflow or MGM. 
 * @author yingfeng
 *
 */
@Entity
public class MediaFile {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
   
    private URI uri;
	HashMap<String, String> externalIds;
}

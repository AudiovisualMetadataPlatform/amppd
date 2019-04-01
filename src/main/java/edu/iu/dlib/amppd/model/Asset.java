package edu.iu.dlib.amppd.model;

import java.net.URI;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Asset represents a file containing either media content of any MIME type or annotation of a media file in pdf/text/json format, 
 * which can be the input/output of a workflow or MGM. 
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
   
    private URI uri;
    private HashMap<String, String> externalIds;
    
}

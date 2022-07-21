package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.jdo.annotations.Unique;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class contains information about an MGM adapter, including properties stored in the corresponding AMP table, 
 * as well as a reference to the corresponding tool in Galaxy.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "toolId", unique = true)
})
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class MgmTool extends AmpObject {

    // tool ID of the corresponding MGM adapter, provided in MGM config in Galaxy; must be unique
    @NotBlank
	@Unique
	@CsvBindByName(column = "ToolId")
    private String toolId;	

	// long description providing help info for the MGM (not the MGM description from its config xml in Galaxy);
	// since Galaxy API response does not include the help text from the MGM config, we need to store help info on AMP side 
    @Column(columnDefinition="TEXT")
	@CsvBindByName(column = "Help")
	private String help; 
	
	// underlying main module/package/model dependency required by the MGM adapter, 
    // corresponding roughly to the main requirement (not tool name) in MGM config in Galaxy
	@CsvBindByName(column = "Module")
    private String module;	

    /* TODO
     * It's possible that an MGM could depend on multiple modules, for now we care only about the main one;
     * if in the future we need to trace multiple other dependencies, we can move module field to MgmVersion. 
     */    

    // version upgrade info for the main dependency module
	@OneToMany(mappedBy="mgm", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="versions")
    private Set<MgmVersion> versions;
    
	// reference to the corresponding MGM adapter tool in Galaxy,
	// serving as a cache to store Tool instance retrieved from Galaxy API call
	@Transient
	private Tool tool;	
	
}

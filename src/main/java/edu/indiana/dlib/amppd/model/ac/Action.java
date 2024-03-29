package edu.indiana.dlib.amppd.model.ac;



import java.util.Set;

import javax.jdo.annotations.Unique;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonBackReference;

import edu.indiana.dlib.amppd.model.AmpObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * An action typically corresponds to an endpoint mapped to some particular URL pattern.
 * @author yingfeng
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "name", unique = true),
		@Index(columnList = "configurable"),
		@Index(columnList = "actionType"),
		@Index(columnList = "targetType"),
		@Index(columnList = "actionType, targetType", unique = true),
		@Index(columnList = "httpMethod, urlPattern", unique = true),
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Action extends AmpObject {

	public enum ActionType {Create, Read, Update, Activate, Restrict, Move, Delete}
	
	public enum TargetType {  
		Unit, Collection, Item, Primaryfile, Primaryfile_Media, Supplement, Bundle, Batch, Bag, 
		Workflow, WorkflowResult, WorkflowResult_Restricted, WorkflowResult_Output, MgmEvaluationTest,
		AmpUser, Role, Role_Unit, RoleAssignment, RoleAssignment_UnitManager, RoleAssignment_CollectionManager}
	
	@NotBlank
	@Unique
    @Type(type="text")
    private String name;
    
    @Type(type="text")
    private String description;

    // whether role permissions for the action is configurable
    @NotBlank
    private Boolean configurable;    
	
    @NotBlank
	@Enumerated(EnumType.STRING)
    private ActionType actionType;

	@NotBlank
	@Enumerated(EnumType.STRING)
    private TargetType targetType;
    
    @NotBlank
    @Enumerated(EnumType.STRING)
    private HttpMethod httpMethod;
    
    @NotBlank
    @Type(type="text")
    private String urlPattern;    
    
    // permissions: the roles that can perform this action
    @ManyToMany(mappedBy = "actions", fetch = FetchType.LAZY)
	@JsonBackReference(value="roles")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<Role> roles;  
    
}


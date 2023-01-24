package edu.indiana.dlib.amppd.model.ac;



import java.util.Set;

import javax.jdo.annotations.Unique;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
		@Index(columnList = "actionType, targetType", unique = true),
		@Index(columnList = "httpMethod, urlPattern", unique = true),
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Action extends AmpObject {

	public enum ActionType { Create, Read, Query, Execute, Update, Activate, Restrict, Delete }
	
	public enum TargetType {  
		Unit, Collection, Item, Primaryfile, 
		Supplement, UnitSupplement, CollectionSupplement, ItemSupplement, PrimaryfileSupplement, 
		Batch, Job, Bag,
		Workflow, WorkflowResult, 
		Evaluation, EvaluationResult,
		AmpUser, Role, RoleAction, RoleAssignment}
	
	@NotBlank
	@Unique
    @Type(type="text")
    private String name;
    
    @Type(type="text")
    private String description;

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
    @ManyToMany(mappedBy = "actions")
	@JsonBackReference(value="roles")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<Role> roles;  
    
}


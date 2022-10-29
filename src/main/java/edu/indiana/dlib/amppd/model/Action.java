package edu.indiana.dlib.amppd.model;



import java.util.Set;

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

import com.fasterxml.jackson.annotation.JsonBackReference;

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
		@Index(columnList = "actionType", unique = true),
		@Index(columnList = "targetType", unique = true),
		@Index(columnList = "urlPattern", unique = true),
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Action extends AmpObject {

	public enum ActionType { Create, Read, Update, Delete, Execute }
	
	public enum TargetType {  
		Unit, Collection, Item, Primaryfile, Supplement,
		Workflow, WorkflowResult, EvaluationResult,
		Batch, Evaluation,
		AmpUser, Role, RoleAction, RoleAssignment}
	
	@NotBlank
	@Enumerated(EnumType.STRING)
    private ActionType actionType;

	@NotBlank
	@Enumerated(EnumType.STRING)
    private TargetType targetType;
    
    @Type(type="text")
    private String description;
    
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


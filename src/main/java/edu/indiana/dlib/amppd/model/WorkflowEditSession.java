package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Information related to a workflow edit session by a authenticated AMP user on a particular workflow.
 * @author yingfeng
 */
@Entity
@Data
public class WorkflowEditSession {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

	@Index(unique="true")
    @OneToOne(targetEntity = AmpUser.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private AmpUser user;

}

package edu.indiana.dlib.amppd.model;

import javax.jdo.annotations.Index;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class contains information about an MGM Evaluation Test (MET), i.e. an execution of the associated MGM Scoring Tool
 * to compute evaluation scores on a workflow result against its associated ground truth.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@NoArgsConstructor
public class MgmEvaluationTest {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
           
	//@NotNull
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String parameters; // JSON representation of the MET parameters map as <name, value> pairs 
//    private Map<String, Object> parameters; // <name, value> map of the parameters of the MET

    private String scorePath;   // path of the output JSON score file, relative to the score root directory

    // Note: The scores can also be stored in formats other than JSON, (ex. CSV or binary array of float numbers), depending on the need of visualization tools
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String scores;   // JSON representation of the output scores
    
	@NotNull
	@Index
	@ManyToOne
	private MgmScoringTool mst; // MGM scoring tool used by this test
    
	@NotNull
	@Index
	@ManyToOne
    private PrimaryfileSupplement groundTruth;	// the groundtruth used by this test, uploaded as a PrimaryfileSupplement
    
	@NotNull
	@Index
	@ManyToOne
    private WorkflowResult workflowResult; // the workflow result evaluated by this test

}

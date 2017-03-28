package gov.ca.emsa.pulse.broker.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "audit_source")
public class AuditSourceEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "id", nullable = false )
	private Long id;
	
	@Column(name="audit_source_id")
	private String auditSourceId;
	
	@Column(name = "audit_enterprise_site_id")
	private String auditEnterpriseSiteId;
	
	@Column(name = "audit_source_type_code")
	private String auditSourceTypeCode;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAuditSourceId() {
		return auditSourceId;
	}

	public void setAuditSourceId(String auditSourceId) {
		this.auditSourceId = auditSourceId;
	}

	public String getAuditEnterpriseSiteId() {
		return auditEnterpriseSiteId;
	}

	public void setAuditEnterpriseSiteId(String auditEnterpriseSiteId) {
		this.auditEnterpriseSiteId = auditEnterpriseSiteId;
	}

	public String getAuditSourceTypeCode() {
		return auditSourceTypeCode;
	}

	public void setAuditSourceTypeCode(String auditSourceTypeCode) {
		this.auditSourceTypeCode = auditSourceTypeCode;
	}
	
}

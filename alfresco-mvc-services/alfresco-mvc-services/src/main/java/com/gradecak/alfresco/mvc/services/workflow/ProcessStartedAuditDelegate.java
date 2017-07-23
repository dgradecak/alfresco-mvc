package com.gradecak.alfresco.mvc.services.workflow;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.services.service.TenantEntityContainerService;
import com.gradecak.alfresco.mvc.services.service.TenantWorkflowService;

public class ProcessStartedAuditDelegate extends BaseJavaDelegate implements ExecutionListener {

  private final TenantEntityContainerService entityContainerService;

  @Autowired
  public ProcessStartedAuditDelegate(TenantEntityContainerService entityContainerService) {
    Assert.notNull(entityContainerService);

    this.entityContainerService = entityContainerService;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    // NOT used
  }

  @Override
  public void notify(DelegateExecution execution) {

    Map<String, Object> processVariables = execution.getVariables();

    String entity = (String) processVariables.get(TenantWorkflowService.IBAPP_ENTITY_FIELD);
    String primaryKey = (String) processVariables.get(TenantWorkflowService.IBAPP_PRIMARYKEY_FIELD);

    if (StringUtils.hasText(entity) && StringUtils.hasText(primaryKey)) {
      NodeRef packageRef = (NodeRef) processVariables.get(TenantWorkflowService.RELATED_FIELD);
      if (packageRef != null) {
        String description = (String) processVariables.get(TenantWorkflowService.IBAPP_WORKFLOW_DESCRIPTION);
        Assert.hasText(description);

        entityContainerService.createSystemNote(packageRef, ImmutableMap.of("type", "workflow", "action", "created", "id", execution.getProcessInstanceId(), "description", description));
      }
    }

    // Map<QName, Serializable> pathProperties = activitiPropertyConverter.getPathProperties(execution.getId());
    //
    // execution.setVariable("sdwf_audit", Boolean.TRUE);
    //
    // NodeRef pckg = (NodeRef) pathProperties.get(WorkflowModel.ASSOC_PACKAGE);
    //
    // Set<QName> types = new HashSet<>();
    // types.add(DocumentModel.TYPE_DOCUMENT);
    // types.add(KnowledgeModel.TYPE_DOCUMENT);
    //
    // List<ChildAssociationRef> childAssocs = getServiceRegistry().getNodeService().getChildAssocs(pckg, types);
    // List<NodeRef> documents = new ArrayList<>();
    // for (ChildAssociationRef childAssociationRef : childAssocs) {
    // NodeRef childRef = childAssociationRef.getChildRef();
    // documents.add(childRef);
    // }
    //
    // auditLoggerService.audit(new WorkflowEvent(execution.getId(), null, pckg, pathProperties, documents,
    // WorkflowEvent.WORKFLOWCREATED));
  }
}

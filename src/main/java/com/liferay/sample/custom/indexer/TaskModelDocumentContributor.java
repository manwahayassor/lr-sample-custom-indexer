package com.liferay.sample.custom.indexer;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

/**
 * Author Manwah AYASSOR
 * Author Fabian BOUCHE
 */

/*
Requires the following additional type mappings
{
    "LiferayDocumentType": {
        "properties": {
            "activityDescription": {
                "index": "true",
                "store": "true",
                "type": "keyword"
            },
            "activityType": {
                "index": "true",
                "store": "true",
                "type": "keyword"
            },
            "activityType": {
                "index": "true",
                "store": "true",
                "type": "keyword"
            },
            "processusId": {
                "index": "true",
                "store": "true",
                "type": "keyword"
            },
            "processusCode": {
                "index": "true",
                "store": "true",
                "type": "keyword"
            }
        }
    }
}
*/

@Component(
        immediate = true,
        property = "indexer.class.name=" + TaskObjectDefinitionsConstants.TASK_OBJECT_INDEXER,
        service = ModelDocumentContributor.class
)
public class TaskModelDocumentContributor implements ModelDocumentContributor<ObjectEntry> {

    private final static Logger LOG = LoggerFactory.getLogger(TaskModelDocumentContributor.class);

    @Override
    public void contribute(Document document, ObjectEntry taskObject) {

        if (TaskObjectDefinitionsConstants.TASK_OBJECT_DEFINITION_ID == taskObject.getObjectDefinitionId()) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("TaskModelDocumentContributor");
            }

            Long activityId = GetterUtil.getLong(
                    taskObject.getValues().get(TaskObjectDefinitionsConstants.TASK_ACTIVITY_RELATIONSHIP)
            );

            if (Validator.isNotNull(activityId) && activityId > 0) {
                try {
                    ObjectEntry activityEntry = _objectEntryLocalService.fetchObjectEntry(activityId);

                    if (activityEntry == null) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("No ObjectEntry found for activityId: {}", activityId);
                        }
                        return;
                    }

                    Map<String, Serializable> activityValues = activityEntry.getValues();

                    String activityType = GetterUtil.getString(activityValues.get("typeActivite"));
                    String activityDescription = _objectEntryLocalService.getTitleValue(
                            TaskObjectDefinitionsConstants.ACTIVITY_OBJECT_DEFINITION_ID, activityId
                    );

                    Long processusId = GetterUtil.getLong(
                            activityValues.get(TaskObjectDefinitionsConstants.ACTIVITY_PROCESSUS_RELATIONSHIP)
                    );

                    if (Validator.isNotNull(processusId) && processusId > 0) {
                        ObjectEntry processusEntry = _objectEntryLocalService.fetchObjectEntry(processusId);

                        if (processusEntry != null) {
                            Map<String, Serializable> processusValues = processusEntry.getValues();
                            String processusCode = GetterUtil.getString(processusValues.get("code"));

                            document.addKeyword("processusId", processusId);
                            document.addKeyword("processusCode", processusCode);

                            if (LOG.isDebugEnabled()) {
                                LOG.debug(
                                        "Indexed Activity Processus : [processusId={}, processusCode={} activityDesc={}]",
                                        processusId, processusCode, activityDescription
                                );
                            }

                        } else if (LOG.isWarnEnabled()) {
                            LOG.warn("No ObjectEntry found for processusId: {}", processusId);
                        }
                    }

                    document.addKeyword("activityType", activityType);
                    document.addKeyword(SearchConstants.ACTIVITY_DESCRIPTION, activityDescription);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                "Indexed Task Activity: [description={}, type={}, taskTitle={}]",
                                activityDescription, activityType, taskObject.getTitleValue()
                        );
                    }

                } catch (PortalException e) {
                    LOG.error("Error retrieving related ObjectEntry for activityId: {}", activityId, e);
                }
            }
        }
    }

    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

}

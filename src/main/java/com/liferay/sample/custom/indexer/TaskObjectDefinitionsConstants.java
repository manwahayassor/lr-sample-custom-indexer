package com.liferay.sample.custom.indexer;

public class TaskObjectDefinitionsConstants {
	
	public static final long TASK_OBJECT_DEFINITION_ID = 114769;
	public static final String TASK_OBJECT_INDEXER = "com.liferay.object.model.ObjectDefinition#" + TASK_OBJECT_DEFINITION_ID;

    public static final long ACTIVITY_OBJECT_DEFINITION_ID = 114710;

    public static final String TASK_ACTIVITY_RELATIONSHIP = "r_activityToTask_c_activityId";
    public static final String ACTIVITY_PROCESSUS_RELATIONSHIP = "r_processusToActivity_c_processusId";
}

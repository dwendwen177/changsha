package org.changsha.changshapoc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionTrace {

    String actionAlias;

    Long actionId;

    String actionType;

    String applicationName;

    String bizSystemName;

    String instanceName;

    String apmData;

}

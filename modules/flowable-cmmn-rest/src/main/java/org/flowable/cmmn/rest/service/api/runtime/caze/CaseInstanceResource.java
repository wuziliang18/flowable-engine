/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.cmmn.rest.service.api.runtime.caze;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.flowable.cmmn.api.CmmnMigrationService;
import org.flowable.cmmn.api.StageResponse;
import org.flowable.cmmn.api.migration.CaseInstanceMigrationDocument;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.engine.CmmnEngineConfiguration;
import org.flowable.cmmn.engine.impl.migration.CaseInstanceMigrationDocumentConverter;
import org.flowable.cmmn.rest.service.api.RestActionRequest;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
@RestController
@Api(tags = { "Case Instances" }, description = "Manage Case Instances", authorizations = { @Authorization(value = "basicAuth") })
public class CaseInstanceResource extends BaseCaseInstanceResource {
    
    @Autowired
    protected CmmnEngineConfiguration cmmnEngineConfiguration;

    @Autowired
    protected CmmnMigrationService cmmnMigrationService;

    @ApiOperation(value = "Get a case instance", tags = { "Case Instances" }, nickname = "getCaseInstance")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Indicates the case instance was found and returned."),
            @ApiResponse(code = 404, message = "Indicates the requested case instance was not found.")
    })
    @GetMapping(value = "/cmmn-runtime/case-instances/{caseInstanceId}", produces = "application/json")
    public CaseInstanceResponse getCaseInstance(@ApiParam(name = "caseInstanceId") @PathVariable String caseInstanceId, HttpServletRequest request) {
        CaseInstanceResponse caseInstanceResponse = restResponseFactory.createCaseInstanceResponse(getCaseInstanceFromRequest(caseInstanceId));
        
        CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().caseDefinitionId(caseInstanceResponse.getCaseDefinitionId()).singleResult();
        if (caseDefinition != null) {
            caseInstanceResponse.setCaseDefinitionName(caseDefinition.getName());
            caseInstanceResponse.setCaseDefinitionDescription(caseDefinition.getDescription());
        }
        
        return caseInstanceResponse;
    }
    
    @ApiOperation(value = "Update case instance properties or execute an action on a case instance (body needs to contain an 'action' property for the latter).", tags = { "Plan Item Instances" }, notes = "")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Indicates the case instance was found and the action/update is performed."),
            @ApiResponse(code = 204, message = "Indicates the case was found, the change was performed and it caused the case instance to end."),
            @ApiResponse(code = 400, message = "Indicates an illegal parameter was passed, required parameters are missing in the request body or illegal variables are passed in. Status description contains additional information about the error."),
            @ApiResponse(code = 404, message = "Indicates the case instance was not found.")
    })
    @PutMapping(value = "/cmmn-runtime/case-instances/{caseInstanceId}", produces = "application/json")
    public CaseInstanceResponse updateCaseInstance(@ApiParam(name = "caseInstanceId") @PathVariable String caseInstanceId,
                    @RequestBody CaseInstanceUpdateRequest updateRequest, HttpServletRequest request, HttpServletResponse response) {

        CaseInstance caseInstance = getCaseInstanceFromRequest(caseInstanceId);

        if (StringUtils.isNotEmpty(updateRequest.getAction())) {

            if (restApiInterceptor != null) {
                restApiInterceptor.doCaseInstanceAction(caseInstance, updateRequest);
            }

            if (RestActionRequest.EVALUATE_CRITERIA.equals(updateRequest.getAction())) {
                runtimeService.evaluateCriteria(caseInstance.getId());

            } else {
                throw new FlowableIllegalArgumentException("Invalid action: '" + updateRequest.getAction() + "'.");
            }

        } else { // regular update

            if (restApiInterceptor != null) {
                restApiInterceptor.updateCaseInstance(caseInstance, updateRequest);
            }

            if (StringUtils.isNotEmpty(updateRequest.getName())) {
                runtimeService.setCaseInstanceName(caseInstanceId, updateRequest.getName());
            }
            if (StringUtils.isNotEmpty(updateRequest.getBusinessKey())) {
                runtimeService.updateBusinessKey(caseInstanceId, updateRequest.getBusinessKey());
            }

        }

        // Re-fetch the case instance, could have changed due to action or even completed
        caseInstance = runtimeService.createCaseInstanceQuery().caseInstanceId(caseInstance.getId()).singleResult();
        if (caseInstance == null) {
            // Case instance is finished, return empty body to inform user
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return null;
        } else {
            return restResponseFactory.createCaseInstanceResponse(caseInstance);
        }

    }

    @ApiOperation(value = "Terminate a case instance", tags = { "Case Instances" }, nickname = "terminateCaseInstance")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Indicates the case instance was found and terminate. Response body is left empty intentionally."),
            @ApiResponse(code = 404, message = "Indicates the requested case instance was not found.")
    })
    @DeleteMapping(value = "/cmmn-runtime/case-instances/{caseInstanceId}")
    public void terminateCaseInstance(@ApiParam(name = "caseInstanceId") @PathVariable String caseInstanceId, HttpServletResponse response) {
        CaseInstance caseInstance = getCaseInstanceFromRequest(caseInstanceId);
        
        if (restApiInterceptor != null) {
            restApiInterceptor.terminateCaseInstance(caseInstance);
        }

        runtimeService.terminateCaseInstance(caseInstance.getId());
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }
    
    @ApiOperation(value = "Delete a case instance", tags = { "Case Instances" }, nickname = "deleteCaseInstance")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Indicates the case instance was found and deleted. Response body is left empty intentionally."),
            @ApiResponse(code = 404, message = "Indicates the requested case instance was not found.")
    })
    @DeleteMapping(value = "/cmmn-runtime/case-instances/{caseInstanceId}/delete")
    public void deleteCaseInstance(@ApiParam(name = "caseInstanceId") @PathVariable String caseInstanceId, HttpServletResponse response) {
        CaseInstance caseInstance = getCaseInstanceFromRequest(caseInstanceId);
        
        if (restApiInterceptor != null) {
            restApiInterceptor.deleteCaseInstance(caseInstance);
        }

        runtimeService.deleteCaseInstance(caseInstance.getId());
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    @GetMapping(value = "/cmmn-runtime/case-instances/{caseInstanceId}/stage-overview", produces = "application/json")
    public List<StageResponse> getStageOverview(@ApiParam(name = "caseInstanceId") @PathVariable String caseInstanceId) {

        CaseInstance caseInstance = runtimeService.createCaseInstanceQuery().caseInstanceId(caseInstanceId).singleResult();

        if (caseInstance == null) {
            throw new FlowableObjectNotFoundException("No case instance found for id " + caseInstanceId);
        }

        if (restApiInterceptor != null) {
            restApiInterceptor.accessStageOverview(caseInstance);
        }

        return runtimeService.getStageOverview(caseInstanceId);
    }
    
    @ApiOperation(value = "Change the state of a case instance", tags = { "Case Instances" }, notes = "")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Indicates the case instance was found and change state activity was executed."),
            @ApiResponse(code = 404, message = "Indicates the requested case instance was not found.")
    })
    @PostMapping(value = "/runtime/case-instances/{caseInstanceId}/change-state", produces = "application/json")
    public void changePlanItemState(@ApiParam(name = "caseInstanceId") @PathVariable String caseInstanceId,
            @RequestBody ChangePlanItemStateRequest planItemStateRequest, HttpServletRequest request) {
        
        if (restApiInterceptor != null) {
            restApiInterceptor.changePlanItemState(caseInstanceId, planItemStateRequest);
        }

        if (planItemStateRequest.getActivatePlanItemDefinitionIds() != null && !planItemStateRequest.getActivatePlanItemDefinitionIds().isEmpty()) {
            runtimeService.createChangePlanItemStateBuilder()
                .caseInstanceId(caseInstanceId)
                .activatePlanItemDefinitionIds(planItemStateRequest.getActivatePlanItemDefinitionIds())
                .changeState();
        
        } else if (planItemStateRequest.getMoveToAvailablePlanItemDefinitionIds() != null && !planItemStateRequest.getMoveToAvailablePlanItemDefinitionIds().isEmpty()) {
            runtimeService.createChangePlanItemStateBuilder()
                .caseInstanceId(caseInstanceId)
                .changeToAvailableStateByPlanItemDefinitionIds(planItemStateRequest.getMoveToAvailablePlanItemDefinitionIds())
                .changeState();
        
        } else if (planItemStateRequest.getTerminatePlanItemDefinitionIds() != null && !planItemStateRequest.getTerminatePlanItemDefinitionIds().isEmpty()) {
            runtimeService.createChangePlanItemStateBuilder()
                .caseInstanceId(caseInstanceId)
                .terminatePlanItemDefinitionIds(planItemStateRequest.getTerminatePlanItemDefinitionIds())
                .changeState();
    }
        
    }

    @ApiOperation(value = "Migrate case instance", tags = { "Case Instances" }, notes = "")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Indicates the case instance was found and migration was executed."),
            @ApiResponse(code = 409, message = "Indicates the requested case instance action cannot be executed since the case-instance is already activated/suspended."),
            @ApiResponse(code = 404, message = "Indicates the requested case instance was not found.")
    })
    @PostMapping(value = "/runtime/case-instances/{caseInstanceId}/migrate", produces = "application/json")
    public void migrateCaseInstance(@ApiParam(name = "caseInstanceId") @PathVariable String caseInstanceId,
                                       @RequestBody String migrationDocumentJson, HttpServletRequest request) {

        if (restApiInterceptor != null) {
            restApiInterceptor.migrateCaseInstance(caseInstanceId, migrationDocumentJson);
        }

        CaseInstanceMigrationDocument migrationDocument = CaseInstanceMigrationDocumentConverter.convertFromJson(migrationDocumentJson);
        cmmnMigrationService.migrateCaseInstance(caseInstanceId, migrationDocument);
    }
}

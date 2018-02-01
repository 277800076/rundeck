package rundeck.controllers

import com.dtolabs.rundeck.app.support.trigger.TriggerCreate
import com.dtolabs.rundeck.app.support.trigger.TriggerRequest
import com.dtolabs.rundeck.app.support.trigger.TriggerUpdate
import com.dtolabs.rundeck.core.authorization.UserAndRolesAuthContext
import com.fasterxml.jackson.databind.ObjectMapper
import rundeck.TriggerRep

class TriggerController extends ControllerBase {
    def frameworkService
    def triggerService
    static allowedMethods = [
            'createPost': 'POST',
            'deletePost': 'POST',
            'updatePost': 'POST',
    ]

    def index(String project) {
        //TODO project
        redirect(action: 'list', params: [project: project])
    }

    def list(String project) {
//        def framework = frameworkService.getRundeckFramework()

//        AuthContext authContext = frameworkService.getAuthContextForSubjectAndProject(session.subject,scheduledExecution.project)

        //TODO: auth
        def triggers = TriggerRep.findAllByProject(project)
        [triggers: triggers, project: project]
    }

    def create(String project) {

    }

    def createPost(TriggerCreate input) {
        if (!requestHasValidToken()) {
            return
        }
        input.validate()
        if (input.hasErrors()) {
            request.errors = input.errors
            //TODO: create form
            return renderErrorView([:])
        }
        //TODO: project exists?

        UserAndRolesAuthContext authContext = frameworkService.getAuthContextForSubjectAndProject(session.subject, input.project)

        //TODO: auth
        ObjectMapper mapper = new ObjectMapper()
        //TODO: condition map data
        Map conditionMap = params.conditionData ? mapper.readValue(params.conditionData, Map) : null
        //TODO: action map data
        Map actionMap = params.actionData ? mapper.readValue(params.actionData, Map) : null
        //TODO: trigger user data
        Map userData = params.triggerData ? mapper.readValue(params.triggerData, Map) : null



        def rep = triggerService.createTrigger(authContext, input, conditionMap, actionMap, userData)

        return redirect(action: 'show', params: [id: rep.uuid, project: input.project])
    }

    def delete(TriggerRequest input) {

        show(input)
    }

    def deletePost(TriggerRequest input) {
        if (!requestHasValidToken()) {
            return
        }
        //TODO: project exists?
        //TODO: auth
        def trigger = TriggerRep.findByProjectAndUuid(input.project, input.id)
        if (notFoundResponse(trigger, 'Trigger', input.id)) {
            return
        }

        boolean result = triggerService.deleteTrigger(trigger)

        if (result) {
            flash.message = "Trigger $input.id was deleted"
        } else {
            flash.error = "Trigger $input.id was NOT deleted"
        }
        redirect(action: 'list', params: [project: input.project])
    }

    def edit(TriggerRequest input) {
        show(input)
    }

    def updatePost(TriggerUpdate input) {
        if (!requestHasValidToken()) {
            return
        }
        input.validate()
        if (input.hasErrors()) {
            request.errors = input.errors
            //TODO: edit form
            return renderErrorView([:])
        }
        def trigger = TriggerRep.findByProjectAndUuid(input.project, input.id)
        if (notFoundResponse(trigger, 'Trigger', input.id)) {
            return
        }
        ObjectMapper mapper = new ObjectMapper()

        //TODO: condition map data
        Map conditionMap = params.conditionData ? mapper.readValue(params.conditionData, Map) : null
        //TODO: action map data
        Map actionMap = params.actionData ? mapper.readValue(params.actionData, Map) : null
        //TODO: trigger user data
        Map userData = params.triggerData ? mapper.readValue(params.triggerData, Map) : null
        //TODO...

        //TODO; auth

        UserAndRolesAuthContext authContext = frameworkService.getAuthContextForSubjectAndProject(session.subject, input.project)

        triggerService.updateTrigger(authContext, trigger, input, conditionMap, actionMap, userData)
        flash.message = "Trigger updated"
        redirect(action: 'show', params: [project: input.project, id: input.id])
    }

    def show(TriggerRequest input) {

        //TODO: project exists?
        //TODO: auth?

        def trigger = TriggerRep.findByProjectAndUuid(input.project, input.id)
        if (notFoundResponse(trigger, 'Trigger', input.id)) {
            return
        }

        [trigger: trigger, project: input.project]
    }

    def test(TriggerRequest input) {
        def trigger = TriggerRep.findByProjectAndUuid(input.project, input.id)
        if (notFoundResponse(trigger, 'Trigger', input.id)) {
            return
        }

        triggerService.triggerConditionMet(input.id, params.data ?: [:])
        flash.message = "Trigger started"
        redirect(action: 'show', params: params)
    }
}
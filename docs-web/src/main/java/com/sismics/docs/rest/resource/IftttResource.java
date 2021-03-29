package com.sismics.docs.rest.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sismics.docs.core.constant.*;
import com.sismics.docs.core.dao.*;
import com.sismics.docs.core.dao.criteria.RouteModelCriteria;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.dto.RouteModelDto;
import com.sismics.docs.core.dao.dto.TagDto;
import com.sismics.docs.core.event.DocumentEvent;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;
import com.sismics.docs.core.model.jpa.*;
import com.sismics.docs.core.util.ActionUtil;
import com.sismics.docs.core.util.IftttUtil;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.AclUtil;
import com.sismics.rest.util.ValidationUtil;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Path("/ifttt")
public class IftttResource extends BaseResource {

    @PUT
    @Path("rules")
    public Response add(@FormParam("name") String name, @FormParam("rule") String ruleData) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate input
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        ruleData = ValidationUtil.validateLength(ruleData, "rule", 1, 5000, false);

        // Create the rule model
        IftttDao iftttDao = new IftttDao();

        IftttRule rule = new IftttRule().setName(name).setRule(ruleData);
        IftttRuleModel ruleModel = IftttUtil.getRuleModel(rule);
        rule.setRule(IftttUtil.convertRuleModel(ruleModel));

        IftttUtil.validateModel(ruleModel);

        String id = iftttDao.create(rule, principal.getId());

        List<Class> triggers = IftttUtil.getTriggers(ruleModel);
        setTriggerForRule(rule, triggers);

        // Create read ACL
        AclDao aclDao = new AclDao();
        Acl acl = new Acl();
        acl.setPerm(PermType.READ);
        acl.setType(AclType.USER);
        acl.setSourceId(id);
        acl.setTargetId(principal.getId());
        aclDao.create(acl, principal.getId());

        // Create write ACL
        acl = new Acl();
        acl.setPerm(PermType.WRITE);
        acl.setType(AclType.USER);
        acl.setSourceId(id);
        acl.setTargetId(principal.getId());
        aclDao.create(acl, principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id);
        return Response.ok().entity(response.build()).build();
    }

    @POST
    @Path("rules/{id: [a-z0-9\\-]+}")
    public Response update(@PathParam("id") String id,
                           @FormParam("name") String name,
                           @FormParam("rule") String rule) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate input
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        rule = ValidationUtil.validateLength(rule, "rule", 1, 5000, false);

        // Get the rule model
        IftttDao dao = new IftttDao();
        IftttRule ruleModel = dao.getActiveById(id);
        if (ruleModel == null) {
            throw new NotFoundException();
        }

        // Update the rule model
        dao.update(ruleModel.setName(name)
                .setRule(rule), principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }


    @DELETE
    @Path("rules/{id: [a-z0-9\\-]+}")
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get the rule model
        IftttDao dao = new IftttDao();
        IftttRule ruleModel = dao.getActiveById(id);
        if (ruleModel == null) {
            throw new NotFoundException();
        }

        // Delete the rule model
        dao.deleteRule(ruleModel.getId(), principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    @GET
    @Path("rules/{id: [a-z0-9\\-]+}")
    public Response get(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get the rule model
        IftttDao dao = new IftttDao();
        IftttRule ruleModel = dao.getActiveById(id);
        if (ruleModel == null) {
            throw new NotFoundException();
        }

        // Build the response
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", ruleModel.getId())
                .add("name", ruleModel.getName())
                .add("create_date", ruleModel.getCreateDate().getTime())
                .add("rule", ruleModel.getRule());

        // Add ACL
        AclUtil.addAcls(response, id, getTargetIdList(null));

        return Response.ok().entity(response.build()).build();
    }

    @GET
    @Path("/rules")
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        IftttDao dao = new IftttDao();
        List<IftttRule> rules = dao.findAll();

        // Build the response
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (IftttRule rule : rules) {
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("id", rule.getId())
                    .add("name", rule.getName())
                    .add("create_date", rule.getCreateDate().getTime())
                    .add("rule", rule.getRule());
            items.add(response);
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("rules", items);
        return Response.ok().entity(response.build()).build();
    }

    private void setTriggerForRule(IftttRule rule, List<Class> triggers) {
        IftttTriggerDao triggerDao = new IftttTriggerDao();
        IftttTriggerForRuleDao triggerForRuleDao = new IftttTriggerForRuleDao();
        triggerForRuleDao.deleteTriggerForRule(rule.getId());

        triggers.forEach(trigger -> {
            IftttTrigger triggerEntity = triggerDao.getTriggerByName(trigger.getCanonicalName());
            if ( triggerEntity == null ) {
                triggerEntity = new IftttTrigger();
                triggerEntity.setName(trigger.getCanonicalName());
                String id = triggerDao.create(triggerEntity,principal.getId());
            }
            IftttTriggerForRule triggerForRule = new IftttTriggerForRule();
            triggerForRule.setRuleId(rule.getId());
            triggerForRule.setTriggerId(triggerEntity.getId());
            triggerForRuleDao.create(triggerForRule);
        });
    }

}

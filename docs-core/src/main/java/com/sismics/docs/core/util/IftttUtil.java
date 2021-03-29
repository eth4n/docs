package com.sismics.docs.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sismics.docs.core.dao.IftttDao;
import com.sismics.docs.core.model.jpa.IftttRule;
import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import com.sismics.docs.core.util.ifttt.action.CallWebhook;
import com.sismics.docs.core.util.ifttt.action.SetDocumentTitle;
import com.sismics.docs.core.util.ifttt.condition.ConditionResult;
import com.sismics.docs.core.util.ifttt.condition.AbstractCondition;
import com.sismics.docs.core.util.ifttt.condition.DocumentProperty;
import com.sismics.docs.core.util.ifttt.action.AbstractAction;
import com.sismics.docs.core.util.ifttt.action.AddTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class IftttUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IftttUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static Map<String, AbstractCondition> conditions = new HashMap<>();
    private static Map<String, AbstractAction> actions = new HashMap<>();

    /**
     * Registration of conditions and actions
     */
    static {
        // -------------
        conditions.put(DocumentProperty.class.getSimpleName(), new DocumentProperty());

        // -------------
        actions.put(AddTag.class.getSimpleName(), new AddTag());
        actions.put(CallWebhook.class.getSimpleName(), new CallWebhook());
        actions.put(SetDocumentTitle.class.getSimpleName(), new SetDocumentTitle());

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static Map<String, AbstractCondition> getConditions() {
        return conditions;
    }

    public static Map<String, AbstractAction> getActions() {
        return actions;
    }

    /**
     * Deserializing the rule definition from the entity.
     * @param rule
     * @return
     * @throws JsonProcessingException
     */
    public static IftttRuleModel getRuleModel(IftttRule rule) throws JsonProcessingException {
        return objectMapper.readValue(rule.getRule(), IftttRuleModel.class);
    }

    /**
     * Serializing the given rule definition to a string to be stored in an entity.
     * @param model
     * @return
     * @throws JsonProcessingException
     */
    public static String convertRuleModel(IftttRuleModel model) throws JsonProcessingException {
        return objectMapper.writeValueAsString(model);
    }

    /**
     * Validation of a rule definition model. Each condition and action in the model is validated.
     * All conditions do need to share a common trigger. Actions and conditions to need to share a
     * common trigger which can be consumed by the actions.
     *
     * @param model
     * @throws Exception
     */
    public static void validateModel(IftttRuleModel model) throws Exception {
        List<Class> conditionTrigger = new ArrayList<>();

        // Validate each condition separately
        for (IftttRuleModel.ConditionData conditionData : (Iterable<IftttRuleModel.ConditionData>) model.getConditions()::iterator) {
            AbstractCondition condition = getCondition(conditionData);
            condition.validate(conditionData);
            Set<Class> triggers = condition.getTriggers(conditionData);
            if ( !conditionTrigger.isEmpty() && getOverlappingEntries(conditionTrigger, triggers).isEmpty()) {
                throw new Exception("Condition "+ conditionData.getCondition() +" does not share a common trigger with previous conditions");
            }
            conditionTrigger.addAll(triggers);
        }

        // Validate each action separately
        for (IftttRuleModel.ActionData actionData : (Iterable<IftttRuleModel.ActionData>) model.getActions()::iterator) {
            AbstractAction action = getAction(actionData);
            action.validate(actionData);
            Set<Class> consumes = action.consumes(actionData);
            if ( getOverlappingEntries(conditionTrigger, consumes).isEmpty()) {
                throw new Exception("Conditions do not provide required input for action "+ actionData.getAction());
            }
        }
    }

    private static List<Class> getOverlappingEntries(Collection<Class> a, Collection<Class> b) {
        return a.stream()
                .filter(b::contains)
                .collect(Collectors
                        .toList());
    }

    /**
     * Get implementation
     *
     * @param conditionData
     * @return
     */
    public static AbstractCondition getCondition(IftttRuleModel.ConditionData conditionData) {
        return conditions.get(conditionData.getCondition());
    }

    /**
     * Get action implementation
     *
     * @param actionData
     * @return
     */
    public static AbstractAction getAction(IftttRuleModel.ActionData actionData) {
        return actions.get(actionData.getAction());
    }

    /**
     * Collect all triggers for a given Ifttt rule definition
     *
     * @param model
     * @return
     */
    public static List<Class> getTriggers(IftttRuleModel model) {
        return model.getConditions().stream().map(ifAndData -> {
            AbstractCondition condition = getCondition(ifAndData);
            return condition.getTriggers(ifAndData);
        }).flatMap(Set::stream).collect(Collectors.toList());
    }

    /**
     * Converts given class to trigger name (class name with package) including
     * all parent classes.
     *
     * @param trigger
     * @return
     */
    private static List<String> toTriggerNames(Class trigger) {
        List<String> triggerNames = new ArrayList<>();

        triggerNames.add(trigger.getCanonicalName());
        if ( trigger.getSuperclass() == null ) {
            return triggerNames;
        }
        triggerNames.addAll(toTriggerNames(trigger.getSuperclass()));

        return triggerNames;
    }

    /**
     * Adds given object to the context under given class and under the class' parent classes
     *
     * @param objClass
     * @param obj
     * @param ctx
     * @return
     */
    private static IftttContext addToContext(Class objClass, Object obj, IftttContext ctx) {
        ctx.addToCtx(objClass, obj);

        if ( objClass.getSuperclass() == null ) {
            return ctx;
        }
        return addToContext(objClass.getSuperclass(), obj, ctx);
    }

    /**
     * Main execution method to run If-this-then-that rules using given trigger.
     *
     * Each persisted rule can listen on trigger objects (usually events from the application).
     * All rules for given trigger are loaded from the db and the conditions are executed. If all
     * conditions validated to <strong>true</strong>, all actions are processed.
     *
     * Method does not produce new events on its own to prevent undesired loops.
     *
     * @param trigger
     */
    public static void executeTrigger(Object trigger) {
        String triggerName = trigger.getClass().getSimpleName();
        List<String> triggerNames = toTriggerNames(trigger.getClass());
        IftttDao dao = new IftttDao();

        List<IftttRule> rules = dao.getActiveByTrigger(triggerNames);
        log.info("Executing "+ rules.size() +" Ifttt-rules for trigger "+ triggerName +" ("+ String.join(",", triggerNames) +")");

        rules.forEach( rule -> {
            try {
                IftttRuleModel model = getRuleModel(rule);
                IftttContext ctx = addToContext(trigger.getClass(), trigger, new IftttContext());

                boolean conditionsApplicable = true;
                for (IftttRuleModel.ConditionData conditionData : (Iterable<IftttRuleModel.ConditionData>) model.getConditions()::iterator) {
                    AbstractCondition ifModel = getCondition(conditionData);

                    ConditionResult result = ifModel.getConditionResult(conditionData, ctx);
                    log.debug("Condition "+ conditionData.getCondition() +" from rule "+ rule.getId() +" is "+ (result.isResult() ? "":"not ") +"applicable for trigger "+ triggerName);

                    conditionsApplicable &= result.isResult();
                    if ( result.isResult() ) {
                        addToContext(result.getClass(), result, ctx);
                    }
                }

                if ( conditionsApplicable ) {
                    model.getActions().forEach(actionData -> {
                        AbstractAction thenModel = getAction(actionData);
                        log.debug("Processing action "+ actionData.getAction() +" of rule "+ rule.getId() +" for trigger "+ triggerName);
                        thenModel.process(actionData, ctx);
                    });
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }
}

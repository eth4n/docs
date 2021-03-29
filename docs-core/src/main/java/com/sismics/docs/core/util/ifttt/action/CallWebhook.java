package com.sismics.docs.core.util.ifttt.action;

import com.sismics.docs.core.event.DocumentEvent;
import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * Action for the If-this-then-that system to call a webhook
 *
 * Requires a document event (create or update). Sends a POST, PUT or GET request
 * to a given URL. POST and PUT will send a body of form:
 * {
 *     rule: "$ruleName",
 *     id: $documentId
 * }
 *
 * Get requests will have query parameter added to the call:
 * ?rule=$ruleName&documentId=$documentId
 */
public class CallWebhook extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(CallWebhook.class);
    public static final String DATA_KEY_METHOD = "method";
    public static final String DATA_KEY_URL = "url";

    /**
     * OkHttp client.
     */
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    public Set<Class> consumes(IftttRuleModel.ActionData actionData) {
        Set<Class> set = new HashSet<>();
        set.add(DocumentEvent.class);
        return set;
    }

    @Override
    public void process(IftttRuleModel.ActionData actionData, IftttContext ctx) {
        String url = (String) getData(actionData, DATA_KEY_URL, null);
        String method = (String) getData(actionData, DATA_KEY_METHOD, "POST");

        DocumentEvent documentEvent = ctx.getCtx(DocumentEvent.class, null);
        if (documentEvent == null) {
            log.debug("Action " + getClass().getSimpleName() + " requires a DocumentEvent to work on: No event given");
        } else if (documentEvent.getDocumentId() == null) {
            log.debug("Action " + getClass().getSimpleName() + " requires a Document to work on: Event did contain a null value for documentId");
        } else {

            Request.Builder requestBuilder = new Request.Builder();
            RequestBody body = RequestBody.create(JSON, "{\"rule\": \"" + ctx.getRuleName() + "\", \"id\": \"" + documentEvent.getDocumentId() + "\"}");

            if ( "POST".equalsIgnoreCase(method)) {
                requestBuilder.post(body).url(url);
            } else if ( "PUT".equalsIgnoreCase(method)) {
                requestBuilder.post(body).url(url);
            } else {
                requestBuilder.url( url +(url.contains("?") ? "&":"?") +"rule="+ ctx.getRuleName() +"&documentId="+documentEvent.getDocumentId());
            }
            Request request = requestBuilder.build();
            try (Response response = client.newCall(request).execute()) {
                log.info("Successfully called the webhook at: " + request.url() + " - " + response.code());
            } catch (IOException e) {
                log.error("Error calling the webhook at: " + request.url(), e.getMessage());
            }
        }
    }


}

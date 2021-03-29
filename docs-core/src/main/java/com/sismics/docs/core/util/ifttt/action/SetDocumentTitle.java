package com.sismics.docs.core.util.ifttt.action;

import com.sismics.docs.core.dao.DocumentDao;
import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.dao.TagDao;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.dto.TagDto;
import com.sismics.docs.core.event.DocumentEvent;
import com.sismics.docs.core.event.FileEvent;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.Tag;
import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import com.sismics.docs.core.util.ifttt.condition.comparator.MatcherConditionResult;
import com.sismics.docs.core.util.jpa.SortCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Action for the If-this-then-that system to set a document's title.
 *
 * Requires a document event (create or update) in order to have a document to set the title.
 *
 * Can work with a MatcherConditionResult to work with matching groups in a regular expression. Use
 * syntax '$n' to access n-th matching group (0 is always the whole matched phrase, so start with $1):
 * @see MatcherConditionResult
 * @see com.sismics.docs.core.util.ifttt.condition.comparator.MatcherComparator
 */
public class SetDocumentTitle extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(SetDocumentTitle.class);
    public static final String DATA_KEY_TITLE = "title";

    @Override
    public Set<Class> consumes(IftttRuleModel.ActionData actionData) {
        Set<Class> set = new HashSet<>();
        set.add(DocumentEvent.class);
        set.add(FileEvent.class);
        return set;
    }

    @Override
    public void process(IftttRuleModel.ActionData actionData, IftttContext ctx) {
        String title = (String) getData(actionData, DATA_KEY_TITLE, null);

        if ( title == null || title.isEmpty() ) {
            log.debug("Misconfigured Ifttt action: No title to set given");
        } else {
            Document document = getDocument(ctx);
            if ( document == null ) {
                log.debug("Action "+ getClass().getSimpleName() +" requires a Document to work on: No document or file event given");
            } else {
                // Enrich title if regex matcher groups are used
                title = enrichTitle(title, actionData, ctx);

                document.setTitle(title);
                DocumentDao dao = new DocumentDao();

                log.debug("Set new title for document "+ document.getId());
                dao.update(document, "admin");
            }
        }
    }

    private String enrichTitle(String title, IftttRuleModel.ActionData actionData, IftttContext ctx) {
        if ( title.contains("$") ) {
            MatcherConditionResult matcherIfApplicableResult = ctx.getCtx(MatcherConditionResult.class, null);
            if ( matcherIfApplicableResult == null ) {
                log.debug("Title to set contains $ character, indicating to enrich the title with a regex matching group: No matcher result found though :(");
            } else {
                log.debug("Title to set contains $ character, indicating to enrich the title with a regex matching group: Matcher result found with "+ matcherIfApplicableResult.getResultGroups().size() +" groups");

                for(int i=0; i < matcherIfApplicableResult.getResultGroups().size(); i++) {
                    title = title.replaceAll("\\$"+ (i+1), matcherIfApplicableResult.getResultGroups().get(i));
                }
            }
        }
        return title;
    }

    private Document getDocument(IftttContext ctx) {
        DocumentEvent documentEvent = ctx.getCtx(DocumentEvent.class, null);
        FileEvent fileEvent = ctx.getCtx(FileEvent.class, null);

        if (documentEvent != null) {
            String documentId = documentEvent.getDocumentId();
            DocumentDao dao = new DocumentDao();
            return dao.getById(documentId);
        } else if (fileEvent != null) {
            FileDao fileDao = new FileDao();
            File file = fileDao.getFile(fileEvent.getFileId());
            if ( file != null ) {
                DocumentDao dao = new DocumentDao();
                return dao.getById(file.getDocumentId());
            }
        }
        log.warn("No document or file event provided to extract document from");
        return null;
    }

}

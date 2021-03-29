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
 * Action for the If-this-then-that system to add a tag to a document.
 *
 * Requires a document event (create or update) in order to have a document to add the tag to.
 * If the document does already have the tag, nothing is done. If the tag does not exist yet,
 * a new tag is created.
 * Can work with a MatcherConditionResult to work with matching groups in a regular expression. Use
 * syntax '$n' to access n-th matching group (0 is always the whole matched phrase, so start with $1):
 * @see MatcherConditionResult
 * @see com.sismics.docs.core.util.ifttt.condition.comparator.MatcherComparator
 */
public class AddTag extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(AddTag.class);
    public static final String DATA_KEY_TAG = "tag";

    @Override
    public Set<Class> consumes(IftttRuleModel.ActionData actionData) {
        Set<Class> set = new HashSet<>();
        set.add(DocumentEvent.class);
        set.add(FileEvent.class);
        return set;
    }

    @Override
    public void process(IftttRuleModel.ActionData actionData, IftttContext ctx) {
        String tagName = (String) getData(actionData, DATA_KEY_TAG, null);

        if ( tagName == null || tagName.isEmpty() ) {
            log.debug("Misconfigured Ifttt action: No tag to set given");
        } else {
            Document document = getDocument(ctx);
            if ( document == null ) {
                log.debug("Action "+ getClass().getSimpleName() +" requires a Document to work on: No document or file event given");
            } else {
                tagName = getCleanTagName(tagName);

                // Enrich tagName if regex matcher groups are used
                tagName = enrichTag(tagName, actionData, ctx);

                TagDao dao = new TagDao();

                // Get tag to add
                String tagId = getOrCreateTag(dao, tagName);

                // Get current tags on document
                Set<String> existingTagIds = dao.findByCriteria(new TagCriteria().setDocumentId(document.getId()), new SortCriteria(0, true)).stream().map(dto -> dto.getId()).collect(Collectors.toSet());

                if ( existingTagIds.contains(tagId)) {
                    log.debug("Document "+ document.getId() +" already has tag "+ tagId +": Skipping addition");
                } else {
                    log.info("Document "+ document.getId() +" does not have tag "+ tagId +": Adding tag");
                    existingTagIds.add(tagId);
                    dao.updateTagList(document.getId(), existingTagIds);
                }
            }
        }
    }

    private String enrichTag(String tagName, IftttRuleModel.ActionData actionData, IftttContext ctx) {
        if ( tagName.contains("$") ) {
            MatcherConditionResult matcherIfApplicableResult = ctx.getCtx(MatcherConditionResult.class, null);
            if ( matcherIfApplicableResult == null ) {
                log.debug("Tag to create contains $ character, indicating to enrich the tag's name with a regex matching group: No matcher result found though :(");
            } else {
                log.debug("Tag to create contains $ character, indicating to enrich the tag's name with a regex matching group: Matcher result found with "+ matcherIfApplicableResult.getResultGroups().size() +" groups");

                for(int i=0; i < matcherIfApplicableResult.getResultGroups().size(); i++) {
                    tagName = tagName.replaceAll("\\$"+ (i+1), matcherIfApplicableResult.getResultGroups().get(i));
                }
            }
        }
        return tagName;
    }

    private String getOrCreateTag(TagDao dao, String tagName) {
        Optional<TagDto> optionalTag = dao.findByCriteria(new TagCriteria().setTagName(tagName), new SortCriteria(0, true)).stream().findFirst();
        String tagId;
        if ( !optionalTag.isPresent() ) {
            Tag newTag = new Tag();
            newTag.setUserId("admin");
            newTag.setName(tagName);
            newTag.setColor(getRandomColor());
            log.debug("Tag with name '"+ newTag.getName() +"' does not exist yet: Creating tag");
            tagId = dao.create(newTag, "admin");
        } else {
            tagId = optionalTag.get().getId();
            log.debug("Tag with name '"+ tagName +"' found: "+ tagId);
        }
        return tagId;
    }

    private String getCleanTagName(String tagName) {
        return tagName.replaceAll("\\s", "_").replaceAll(":", "_");
    }

    private String getRandomColor() {
        // create object of Random class
        Random obj = new Random();
        int rand_num = obj.nextInt(0xffffff + 1);
// format it as hexadecimal string and print
        return String.format("#%06x", rand_num);
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

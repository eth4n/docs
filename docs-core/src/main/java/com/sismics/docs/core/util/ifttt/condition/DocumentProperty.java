package com.sismics.docs.core.util.ifttt.condition;

import com.sismics.docs.core.constant.MetadataType;
import com.sismics.docs.core.dao.*;
import com.sismics.docs.core.dao.criteria.MetadataCriteria;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.dto.DocumentMetadataDto;
import com.sismics.docs.core.event.DocumentEvent;
import com.sismics.docs.core.event.FileEvent;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import com.sismics.docs.core.util.jpa.SortCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Condition implementation for a Ifttt rule to check a document property against
 * some value.
 *
 * Possible document properties to check:
 * @see Property
 */
public class DocumentProperty extends ComparingCondition {
    private static final Logger log = LoggerFactory.getLogger(DocumentProperty.class);

    public enum Property {
        DOCUMENT_TAGS(Collections.singletonList(String[].class)),
        DOCUMENT_META_BOOLEAN(Collections.singletonList(Boolean.class)),
        FILE_CONTENT(Collections.singletonList(String.class));

        private List<Class> produces;

        Property(List<Class> produces) {
            this.produces = produces;
        }

        public List<Class> getProduces() {
            return produces;
        }
    }

    public static final String DATA_KEY_PROPERTY = "property";
    public static final String DATA_KEY_METADATA_NAME = "metadata";

    @Override
    protected List<Class> provides(IftttRuleModel.ConditionData conditionData) {
        // condition is triggered on documentEvent, thus always provides document
        List<Class> classes = new ArrayList<>();
        classes.add(Document.class);

        // may also provide other values, depending on property type
        String propertyString = (String) getData(conditionData, DATA_KEY_PROPERTY, null);
        Property property = propertyString != null ? Property.valueOf(propertyString) : null;
        if (property != null) {
            classes.addAll(property.produces);
        }

        return classes;
    }

    @Override
    public ConditionResult getConditionResult(IftttRuleModel.ConditionData conditionData, IftttContext ctx) {
        return getComparatorInstance(conditionData).compare(this, conditionData, ctx);
    }

    @Override
    public Set<Class> getTriggers(IftttRuleModel.ConditionData conditionData) {
        Set<Class> triggers = new HashSet<>();
        triggers.add(DocumentEvent.class);
        triggers.add(FileEvent.class);
        return triggers;
    }

    @Override
    public <T> T getValue(Class<T> type, IftttRuleModel.ConditionData conditionData, IftttContext ctx) {
        String propertyString = (String) getData(conditionData, DATA_KEY_PROPERTY, null);
        Property property = propertyString != null ? Property.valueOf(propertyString) : null;
        if (type == Document.class) {
            return (T) getDocument(ctx);
        } else if (type == String.class) {
            switch (property) {
                case FILE_CONTENT:
                    return (T) String.join(" ", getFiles(ctx).stream().map(file -> file.getContent()).collect(Collectors.toList()));
            }
        } else if (type == String[].class) {
            switch (property) {
                case DOCUMENT_TAGS:
                    return (T) getTags(ctx);
            }
        } else if (type == Boolean.class) {
            switch (property) {
                case DOCUMENT_META_BOOLEAN:
                    String metadataName = (String) getData(conditionData, DATA_KEY_METADATA_NAME, null);
                    return (T) getBooleanMetaData(ctx,metadataName);
            }
        }
        log.warn("Unsupported type " + type.getName() + " to get context value from (property: " + property + ")");
        return null;
    }

    private Boolean getBooleanMetaData(IftttContext ctx, String metadataName) {
        DocumentEvent event = ctx.getCtx(DocumentEvent.class, null);
        if (event != null) {
            String documentId = event.getDocumentId();
            MetadataDao metadataDao = new MetadataDao();
            DocumentMetadataDao dao = new DocumentMetadataDao();

            DocumentMetadataDto metadata = dao.getByDocumentId(documentId).stream()
                    .filter(m -> metadataName.equalsIgnoreCase(metadataDao.getActiveById(m.getMetadataId()).getName()))
                    .findFirst()
                    .orElse(null);

            dao.getByDocumentId(documentId).stream().forEach(m -> {
                log.info("Metadata found: "+ metadataDao.getActiveById(m.getMetadataId()).getName());
            });

            if ( metadata == null ) {
                log.debug("No metadata found for name "+ metadataName +" on document "+ documentId);
                return null;
            } else if ( metadata.getType() != MetadataType.BOOLEAN ) {
                log.info("Metadata with name "+ metadataName +" on document "+ documentId +" is not of type boolean, found "+ metadata.getType());

                return null;
            }

            return metadata.getValue() != null && metadata.getValue().equalsIgnoreCase("true");
        }
        log.warn("No document event provided to extract document meta data from");
        return null;
    }

    private String[] getTags(IftttContext ctx) {
        DocumentEvent event = ctx.getCtx(DocumentEvent.class, null);

        if (event != null) {
            String documentId = event.getDocumentId();
            TagDao dao = new TagDao();
            return dao.findByCriteria(new TagCriteria().setDocumentId(documentId), new SortCriteria(0, true)).stream()
                    .map(t -> t.getName())
                    .collect(Collectors.toList()).toArray(new String[0]);
        }
        log.warn("No document event provided to extract document tags from");
        return null;
    }

    private List<File> getFiles(IftttContext ctx) {
        Document document = getDocument(ctx);

        if (document != null) {
            FileDao dao = new FileDao();
            return dao.getByDocumentId(null, document.getId());
        }
        log.warn("No document provided to extract files from");
        return Collections.emptyList();
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

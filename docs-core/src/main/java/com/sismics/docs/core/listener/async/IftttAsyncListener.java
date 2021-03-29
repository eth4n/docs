package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.event.*;
import com.sismics.docs.core.util.IftttUtil;
import com.sismics.docs.core.util.TransactionUtil;

/**
 * Listener for triggering If-this-then-that rules.
 *
 * @author eth4n
 */
public class IftttAsyncListener {

    private void executeTrigger(Object event) {
        TransactionUtil.handle(() -> IftttUtil.executeTrigger(event));
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final DocumentCreatedAsyncEvent event) {
        executeTrigger(event);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final DocumentUpdatedAsyncEvent event) {
        executeTrigger(event);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final DocumentDeletedAsyncEvent event) {
        executeTrigger(event);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileCreatedAsyncEvent event) {
        executeTrigger(event);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileUpdatedAsyncEvent event) {
        executeTrigger(event);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileDeletedAsyncEvent event) {
        executeTrigger(event);
    }

}

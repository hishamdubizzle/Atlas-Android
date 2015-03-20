package com.layer.atlas.atlasdefault;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.layer.atlas.R;
import com.layer.atlas.viewholder.ConversationViewHolder;
import com.layer.atlas.viewholder.ConversationViewHolderFactory;
import com.layer.sdk.messaging.Conversation;

public class DefaultConversationViewHolderFactory extends ConversationViewHolderFactory {
    /**
     * Request a new ConversationViewHolder with the given view type.
     *
     * @param viewType The type of ViewHolder to create.
     * @return The ViewHolder generated by the ViewHolderFactory.
     */
    @Override
    public ConversationViewHolder createViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new DefaultConversationViewHolder(inflater.inflate(R.layout.atlas_item_conversation, null));
    }

    /**
     * Request the view type for this Conversation.
     *
     * @param conversation The Conversation to determine a view type for.
     * @return The view type supplied by the ViewHolderFactory.
     */
    @Override
    public int getViewType(Conversation conversation) {
        return 1;
    }
}

package com.layer.ui.message;

import android.databinding.Bindable;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.recyclerview.OnItemClickListener;
import com.layer.ui.viewmodel.ItemViewModel;

import java.util.Collections;
import java.util.Set;

public class MessageFooterViewModel extends ItemViewModel<Message>{

    private Set<Identity> mParticipants;

    public MessageFooterViewModel(
            OnItemClickListener<Message> itemClickListener) {
        super(itemClickListener);
    }

    public void setParticipants(Identity identity) {
        mParticipants = Collections.singleton(identity);
    }

    @Bindable
    public Set<Identity> getParticipants() {
        return mParticipants;
    }
}

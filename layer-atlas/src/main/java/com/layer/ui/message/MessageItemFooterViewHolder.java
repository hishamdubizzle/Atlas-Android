package com.layer.ui.message;

import android.databinding.ViewDataBinding;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.databinding.UiMessageItemFooterBinding;
import com.layer.ui.identity.IdentityFormatterImpl;
import com.layer.ui.message.messagetypes.MessageStyle;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.util.Set;

public class MessageItemFooterViewHolder extends
        ItemViewHolder<Message, MessageItemViewModel, ViewDataBinding, MessageStyle> {

    protected ViewGroup mRoot;

    public MessageItemFooterViewHolder(UiMessageItemFooterBinding binding,
            MessageItemViewModel messageItemViewModel, ImageCacheWrapper imageCacheWrapper) {

        super(binding, messageItemViewModel);
        mRoot = binding.swipeable;
        binding.avatar.init(new AvatarViewModelImpl(imageCacheWrapper), new IdentityFormatterImpl());
    }

    public void bind(Set<Identity> users, View mFooterView) {

        mRoot.addView(mFooterView);
        mViewModel.setParticipants(users);
        mViewModel.notifyChange();
        ((UiMessageItemFooterBinding) mBinding).setViewModel(mViewModel);
    }
}

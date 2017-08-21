package com.layer.ui.message;

import android.content.res.Resources;
import android.databinding.ViewDataBinding;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
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

    public void bind(Set<Identity> users, View mFooterView, int avatarViewVisibilityType) {

        mRoot.addView(mFooterView);
        mViewModel.setParticipants(users);
        mViewModel.setAvatarViewVisibilityType(avatarViewVisibilityType);
        int usersSize = users.size();

        if (usersSize > 2) {
            mViewModel.setTypingIndicatorMessageVisibility(View.VISIBLE);
            mViewModel.setMessageFooterAnimationVisibility(View.GONE);
            String firstUser = "", secondUser = "";
            int counter = 0;

            for (Identity user : users) {
                counter++;
                if (counter == 1) {
                    firstUser = user.getDisplayName();
                } else if (counter == 2) {
                    secondUser = user.getDisplayName();
                    break;
                }
            }
            Resources resources = mFooterView.getContext().getResources();
            String typingIndicatorMessage = resources.getQuantityString(R.plurals.layer_ui_typing_indicator_message,
                    usersSize %2, firstUser, secondUser, usersSize %2);
            mViewModel.setTypingIndicatorMessage(typingIndicatorMessage);
        } else {
            mViewModel.setTypingIndicatorMessageVisibility(View.GONE);
            mViewModel.setMessageFooterAnimationVisibility(View.VISIBLE);
        }


        mViewModel.notifyChange();
        ((UiMessageItemFooterBinding) mBinding).setViewModel(mViewModel);
    }
}

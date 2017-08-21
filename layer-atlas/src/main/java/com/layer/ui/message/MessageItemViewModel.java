package com.layer.ui.message;

import android.databinding.Bindable;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.recyclerview.OnItemClickListener;
import com.layer.ui.viewmodel.ItemViewModel;

import java.util.Set;

public class MessageItemViewModel extends ItemViewModel<Message> {

    private boolean mIsClusterSpaceVisible;
    private boolean mIsDisplayName;
    private boolean mIsBindDateTimeForMessage;
    private String mTimeGroupDay;
    private boolean mIsMessageSent;
    private String mSender;
    private Set<Identity> mParticipants;
    private String mRecipientStatus;
    private String mGroupTime;
    private boolean mIsRecipientStatusVisible;
    private boolean mIsMyCellType;
    private int mAvatarViewVisibilityType;
    private int mMessageFooterAnimationVisibility;
    private String mTypingIndicatorMessage;
    private int mTypingIndicatorVisibility;

    public MessageItemViewModel(
            OnItemClickListener<Message> itemClickListener) {
        super(itemClickListener);
    }


    public boolean isClusterSpaceVisible() {
        return mIsClusterSpaceVisible;
    }

    @Bindable
    public int getAvatarVisibility() {
        return mAvatarViewVisibilityType;
    }

    public void setIsClusterSpaceVisible(boolean isClusterSpaceVisible) {
        mIsClusterSpaceVisible = isClusterSpaceVisible;
    }

    @Bindable
    public Set<Identity> getParticipants() {
        return mParticipants;
    }

    public void setParticipants(Set<Identity> identities) {
        mParticipants = identities;
    }

    @Bindable
    public String getTimeGroupDay() {
       return mTimeGroupDay;
    }

    public void setTimeGroupDay(String timeGroupDay) {
        mTimeGroupDay = timeGroupDay;
    }

    @Bindable
    public boolean getShouldBindDateTimeForMessage() {
        return mIsBindDateTimeForMessage;
    }

    public void setShouldBindDateTimeForMessage(boolean isBindDateTimeForMessage) {
        mIsBindDateTimeForMessage = isBindDateTimeForMessage;
    }

    @Bindable
    public String getRecipientStatus() {
        return mRecipientStatus;
    }

    public void setRecipientStatus(String recipientStatus) {
        mRecipientStatus = recipientStatus;
    }

    @Bindable
    public boolean isMessageSent() {
        return mIsMessageSent;
    }

    public void setMessageSent(boolean isMessageSent) {
        mIsMessageSent = isMessageSent;
    }

    @Bindable
    public String getSender() {
        return mSender;
    }

    public void setSender(String sender) {
        mSender = sender;
    }

    public void setShouldShowDisplayName(boolean isDisplayName) {
        mIsDisplayName = isDisplayName;
    }

    @Bindable
    public boolean getShouldShowDisplayName() {
        return mIsDisplayName;
    }

    @Bindable
    public String getGroupTime() {
        return mGroupTime;
    }

    public void setGroupTime(String groupTime) {
        mGroupTime = groupTime;
    }

    @Bindable
    public boolean isRecipientStatusVisible() {
        return mIsRecipientStatusVisible;
    }

    public void setIsRecipientStatusVisible(boolean isRecipientStatusVisible) {
        mIsRecipientStatusVisible = isRecipientStatusVisible;
    }

    public void setMyCellType(boolean isMyCellType) {
        mIsMyCellType = isMyCellType;
    }

    @Bindable
    public boolean isMyCellType() {
        return mIsMyCellType;
    }

    public void setAvatarViewVisibilityType(int avatarViewVisibilityType) {
        mAvatarViewVisibilityType = avatarViewVisibilityType;
    }

    @Bindable
    public int getMessageFooterAnimationVisibility() {
        return mMessageFooterAnimationVisibility;
    }

    public void setMessageFooterAnimationVisibility(int visibility) {
        mMessageFooterAnimationVisibility = visibility;
    }

    public void setTypingIndicatorMessage(String typingIndicatorMessage) {
        mTypingIndicatorMessage = typingIndicatorMessage;
    }

    @Bindable
    public String getTypingIndicatorMessage() {
        return mTypingIndicatorMessage;
    }

    public void setTypingIndicatorMessageVisibility(int typingIndicatorVisibility) {
        mTypingIndicatorVisibility = typingIndicatorVisibility;
    }

    @Bindable
    public int getTypingIndicatorMessageVisibility() {
        return mTypingIndicatorVisibility;
    }
}

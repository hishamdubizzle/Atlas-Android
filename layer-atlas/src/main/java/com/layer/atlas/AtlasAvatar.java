package com.layer.atlas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.layer.atlas.util.AvatarStyle;
import com.layer.atlas.util.Util;
import com.layer.atlas.util.picasso.transformations.CircleTransform;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Presence;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AtlasAvatar can be used to show information about one user, or as a cluster of multiple users.
 *
 * AtlasAvatar uses Picasso to render the avatar image. So, you need to init
 */
public class AtlasAvatar extends View {
    public static final String TAG = AtlasAvatar.class.getSimpleName();

    private final static CircleTransform SINGLE_TRANSFORM = new CircleTransform(TAG + ".single");
    private final static CircleTransform MULTI_TRANSFORM = new CircleTransform(TAG + ".multi");

    private static final Paint PAINT_TRANSPARENT = new Paint();
    private static final Paint PAINT_BITMAP = new Paint();

    private final Paint mPaintInitials = new Paint();
    private final Paint mPaintBorder = new Paint();
    private final Paint mPaintBackground = new Paint();
    private final Paint mPresencePaint = new Paint();
    private final Paint mBackgroundPaint = new Paint();

    private boolean mShouldShowPresence = true;

    // TODO: make these styleable
    private static final int MAX_AVATARS = 3;
    private static final float BORDER_SIZE_DP = 1f;
    private static final float MULTI_FRACTION = 26f / 40f;

    static {
        PAINT_TRANSPARENT.setARGB(0, 255, 255, 255);
        PAINT_TRANSPARENT.setAntiAlias(true);

        PAINT_BITMAP.setARGB(255, 255, 255, 255);
        PAINT_BITMAP.setAntiAlias(true);
    }

    private Picasso mPicasso;
    private Set<Identity> mParticipants = new LinkedHashSet<>();

    // Initials and Picasso image targets by user ID
    private final Map<Identity, ImageTarget> mImageTargets = new HashMap<>();
    private final Map<Identity, String> mInitials = new HashMap<>();
    private final List<ImageTarget> mPendingLoads = new ArrayList<ImageTarget>();

    // Sizing set in setClusterSizes() and used in onDraw()
    private float mOuterRadius;
    private float mInnerRadius;
    private float mCenterX;
    private float mCenterY;
    private float mDeltaX;
    private float mDeltaY;
    private float mTextSize;
    private float mPresenceOuterRadius;
    private float mPresenceInnerRadius;
    private float mPresenceCenterX;
    private float mPresenceCenterY;

    private Rect mRect = new Rect();
    private RectF mContentRect = new RectF();

    public AtlasAvatar(Context context) {
        super(context);
    }

    public AtlasAvatar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AtlasAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AtlasAvatar init(Picasso picasso) {
        mPicasso = picasso;

        mPaintInitials.setAntiAlias(true);
        mPaintInitials.setSubpixelText(true);
        mPaintBorder.setAntiAlias(true);
        mPaintBackground.setAntiAlias(true);

        mPaintBackground.setColor(getResources().getColor(R.color.atlas_avatar_background));
        mPaintBorder.setColor(getResources().getColor(R.color.atlas_avatar_border));
        mPaintInitials.setColor(getResources().getColor(R.color.atlas_avatar_text));

        return this;
    }

    public AtlasAvatar setStyle(AvatarStyle avatarStyle) {
        mPaintBackground.setColor(avatarStyle.getAvatarBackgroundColor());
        mPaintBorder.setColor(avatarStyle.getAvatarBorderColor());
        mPaintInitials.setColor(avatarStyle.getAvatarTextColor());
        mPaintInitials.setTypeface(avatarStyle.getAvatarTextTypeface());
        return this;
    }

    public AtlasAvatar setParticipants(Identity... participants) {
        mParticipants.clear();
        mParticipants.addAll(Arrays.asList(participants));
        update();
        return this;
    }

    /**
     * Enable or disable showing presence information for this avatar. Presence is shown only for
     * single user Avatars. If avatar is a cluster, presence will not be shown.
     *
     * Default is `true`, to show presence.
     *
     * @param shouldShowPresence set to `true` to show presence, `false` otherwise.
     * @return
     */
    public AtlasAvatar setShouldShowPresence(boolean shouldShowPresence) {
        mShouldShowPresence = shouldShowPresence;
        return this;
    }

    /**
     * Returns if `shouldShowPresence` flag is enabled for this avatar.
     *
     * Default is `true`
     *
     * @return `true` if `shouldShowPresence` is set to `true`, `false` otherwise.
     */
    public boolean getShouldShowPresence() {
        return mShouldShowPresence;
    }

    /**
     * Should be called from UI thread.
     */
    public AtlasAvatar setParticipants(Set<Identity> participants) {
        mParticipants.clear();
        mParticipants.addAll(participants);
        update();
        return this;
    }

    public Set<Identity> getParticipants() {
        return new LinkedHashSet<>(mParticipants);
    }

    private void update() {
        // Limit to MAX_AVATARS valid avatars, prioritizing participants with avatars.
        if (mParticipants.size() > MAX_AVATARS) {
            Queue<Identity> withAvatars = new LinkedList<>();
            Queue<Identity> withoutAvatars = new LinkedList<>();
            for (Identity participant : mParticipants) {
                if (participant == null) continue;
                if (!TextUtils.isEmpty(participant.getAvatarImageUrl())) {
                    withAvatars.add(participant);
                } else {
                    withoutAvatars.add(participant);
                }
            }

            mParticipants = new LinkedHashSet<>();
            int numWithout = Math.min(MAX_AVATARS - withAvatars.size(), withoutAvatars.size());
            for (int i = 0; i < numWithout; i++) {
                mParticipants.add(withoutAvatars.remove());
            }
            int numWith = Math.min(MAX_AVATARS, withAvatars.size());
            for (int i = 0; i < numWith; i++) {
                mParticipants.add(withAvatars.remove());
            }
        }

        Diff diff = diff(mInitials.keySet(), mParticipants);
        List<ImageTarget> toLoad = new ArrayList<>();

        List<ImageTarget> recyclableTargets = new ArrayList<ImageTarget>();
        for (Identity removed : diff.removed) {
            mInitials.remove(removed);
            ImageTarget target = mImageTargets.remove(removed);
            if (target != null) {
                mPicasso.cancelRequest(target);
                recyclableTargets.add(target);
            }
        }

        for (Identity added : diff.added) {
            if (added == null) return;
            mInitials.put(added, Util.getInitials(added));

            final ImageTarget target;
            if (recyclableTargets.isEmpty()) {
                target = new ImageTarget(this);
            } else {
                target = recyclableTargets.remove(0);
            }
            target.setUrl(added.getAvatarImageUrl());
            mImageTargets.put(added, target);
            toLoad.add(target);
        }

        // Cancel existing in case the size or anything else changed.
        // TODO: make caching intelligent wrt sizing
        for (Identity existing : diff.existing) {
            if (existing == null) continue;
            mInitials.put(existing, Util.getInitials(existing));

            ImageTarget existingTarget = mImageTargets.get(existing);
            mPicasso.cancelRequest(existingTarget);
            toLoad.add(existingTarget);
        }
        for (ImageTarget target : mPendingLoads) {
            mPicasso.cancelRequest(target);
        }
        mPendingLoads.clear();
        mPendingLoads.addAll(toLoad);

        setClusterSizes();

        // Invalidate the current view, so it refreshes with new value.
        postInvalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed) return;
        setClusterSizes();
    }

    private boolean setClusterSizes() {
        int avatarCount = mInitials.size();
        if (avatarCount == 0) return false;
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) return false;
        boolean hasBorder = (avatarCount != 1);

        int drawableWidth = params.width - (getPaddingLeft() + getPaddingRight());
        int drawableHeight = params.height - (getPaddingTop() + getPaddingBottom());
        float dimension = Math.min(drawableWidth, drawableHeight);
        float density = getContext().getResources().getDisplayMetrics().density;
        float fraction = (avatarCount > 1) ? MULTI_FRACTION : 1;

        mOuterRadius = fraction * dimension / 2f;
        mInnerRadius = mOuterRadius - (density * BORDER_SIZE_DP);

        mTextSize = mInnerRadius * 4f / 5f;
        mCenterX = getPaddingLeft() + mOuterRadius;
        mCenterY = getPaddingTop() + mOuterRadius;

        float outerMultiSize = fraction * dimension;
        mDeltaX = (drawableWidth - outerMultiSize) / (avatarCount - 1);
        mDeltaY = (drawableHeight - outerMultiSize) / (avatarCount - 1);

        // Presence
        mPresenceOuterRadius = mOuterRadius / 3f;
        mPresenceInnerRadius = mInnerRadius / 3f;
        mPresenceCenterX = mCenterX + mOuterRadius - mPresenceOuterRadius;
        mPresenceCenterY = mCenterY + mOuterRadius - mPresenceOuterRadius;

        synchronized (mPendingLoads) {
            if (!mPendingLoads.isEmpty()) {
                int size = Math.round(hasBorder ? (mInnerRadius * 2f) : (mOuterRadius * 2f));
                for (ImageTarget imageTarget : mPendingLoads) {
                    String targetUrl = imageTarget.getUrl();
                    // Handle empty paths just like null paths. This ensures empty paths will go
                    // through the normal Picasso flow and the bitmap is set.
                    if (targetUrl != null && targetUrl.trim().length() == 0) {
                        targetUrl = null;
                    }
                    mPicasso.load(targetUrl)
                            .tag(AtlasAvatar.TAG).noPlaceholder().noFade()
                            .centerCrop().resize(size, size)
                            .transform((avatarCount > 1) ? MULTI_TRANSFORM : SINGLE_TRANSFORM)
                            .into(imageTarget);
                }
                mPendingLoads.clear();
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Clear canvas
        int avatarCount = mInitials.size();
        canvas.drawRect(0f, 0f, canvas.getWidth(), canvas.getHeight(), PAINT_TRANSPARENT);
        if (avatarCount == 0) return;
        boolean hasBorder = (avatarCount != 1);
        float contentRadius = hasBorder ? mInnerRadius : mOuterRadius;

        // Draw avatar cluster
        float cx = mCenterX;
        float cy = mCenterY;
        mContentRect.set(cx - contentRadius, cy - contentRadius, cx + contentRadius, cy + contentRadius);
        for (Map.Entry<Identity, String> entry : mInitials.entrySet()) {
            // Border / background
            if (hasBorder) canvas.drawCircle(cx, cy, mOuterRadius, mPaintBorder);

            // Initials or bitmap
            ImageTarget imageTarget = mImageTargets.get(entry.getKey());
            Bitmap bitmap = (imageTarget == null) ? null : imageTarget.getBitmap();
            if (bitmap == null) {
                String initials = entry.getValue();
                mPaintInitials.setTextSize(mTextSize);
                mPaintInitials.getTextBounds(initials, 0, initials.length(), mRect);
                canvas.drawCircle(cx, cy, contentRadius, mPaintBackground);
                canvas.drawText(initials, cx - mRect.centerX(), cy - mRect.centerY() - 1f, mPaintInitials);
            } else {
                canvas.drawBitmap(bitmap, mContentRect.left, mContentRect.top, PAINT_BITMAP);
            }

            // Presence
            if (mShouldShowPresence && avatarCount == 1) { // Show only for single user avatars
                drawPresence(canvas, entry.getKey());
            }

            // Translate for next avatar
            cx += mDeltaX;
            cy += mDeltaY;
            mContentRect.offset(mDeltaX, mDeltaY);
        }
    }

    private void drawPresence(Canvas canvas, Identity identity) {
        //dubizzle: changed code to draw green circle wrapped by white circle in case of online. and remove presence at all in case of other status: offline,...
        Presence.PresenceStatus currentStatus = identity.getPresenceStatus();
        if (currentStatus == null) {
            return;
        }

        boolean drawPresence = true;
        boolean makeCircleHollow = false;

        switch (currentStatus) {
            case AVAILABLE:
                ///mPresencePaint.setColor(Color.rgb(0x4F, 0xBF, 0x62));
                mPresencePaint.setColor(Color.WHITE);
                makeCircleHollow = true;
                break;
            case AWAY:
                /// mPresencePaint.setColor(Color.rgb(0xF7, 0xCA, 0x40));
                mPresencePaint.setColor(Color.WHITE);
                makeCircleHollow = true;
                break;
            case OFFLINE:
                ///mPresencePaint.setColor(Color.rgb(0x99, 0x99, 0x9c));
                mPresencePaint.setColor(Color.WHITE);
                makeCircleHollow = true;
                break;
            case INVISIBLE:
                ///mPresencePaint.setColor(Color.rgb(0x50, 0xC0, 0x62));
                mPresencePaint.setColor(Color.WHITE);
                makeCircleHollow = true;
                break;
            case BUSY:
                ///mPresencePaint.setColor(Color.rgb(0xE6, 0x44, 0x3F));
                mPresencePaint.setColor(Color.WHITE);
                makeCircleHollow = true;
                break;
            default:
                drawPresence = false;
                break;
        }
        if (drawPresence) {
            switch (currentStatus) {
                case AVAILABLE:

                    // Clear background + create border
                    /// mBackgroundPaint.setColor(Color.WHITE);
                    mBackgroundPaint.setColor(Color.parseColor("#03DDAD"));
                    mBackgroundPaint.setAntiAlias(true);
                    ///canvas.drawCircle(mPresenceCenterX, mPresenceCenterY, mPresenceOuterRadius, mBackgroundPaint);

                    // Draw Presence status
                    mPresencePaint.setAntiAlias(true);
                    canvas.drawCircle(mPresenceCenterX, mPresenceCenterY, mPresenceInnerRadius, mPresencePaint);

                    // Draw hollow if needed
                    if (makeCircleHollow) {
                        ///canvas.drawCircle(mPresenceCenterX, mPresenceCenterY, (mPresenceInnerRadius / 2f), mBackgroundPaint);
                        canvas.drawCircle(mPresenceCenterX, mPresenceCenterY, (.7f*mPresenceInnerRadius ), mBackgroundPaint);
                    }
                    break;


            }

        }
    }

    private static class ImageTarget implements Target {
        private final static AtomicLong sCounter = new AtomicLong(0);
        private final long mId;
        private final AtlasAvatar mCluster;
        private String mUrl;
        private Bitmap mBitmap;

        public ImageTarget(AtlasAvatar cluster) {
            mId = sCounter.incrementAndGet();
            mCluster = cluster;
        }

        public ImageTarget setUrl(String url) {
            mUrl = url;
            return this;
        }

        public String getUrl() {
            return mUrl;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mCluster.invalidate();
            mBitmap = bitmap;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mCluster.invalidate();
            mBitmap = null;
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            mBitmap = null;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ImageTarget target = (ImageTarget) o;
            return mId == target.mId;
        }

        @Override
        public int hashCode() {
            return (int) (mId ^ (mId >>> 32));
        }
    }

    private static Diff diff(Set<Identity> oldSet, Set<Identity> newSet) {
        Diff diff = new Diff();
        for (Identity old : oldSet) {
            if (newSet.contains(old)) {
                diff.existing.add(old);
            } else {
                diff.removed.add(old);
            }
        }
        for (Identity newItem : newSet) {
            if (!oldSet.contains(newItem)) {
                diff.added.add(newItem);
            }
        }
        return diff;
    }

    private static class Diff {
        public List<Identity> existing = new ArrayList<>();
        public List<Identity> added = new ArrayList<>();
        public List<Identity> removed = new ArrayList<>();
    }
}
/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.quickstep;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.anim.AnimatorPlaybackController;
import com.android.launcher3.util.MultiValueAlpha.AlphaProperty;
import com.android.quickstep.util.RemoteAnimationProvider;
import com.android.quickstep.util.RemoteAnimationTargetSet;
import com.android.systemui.shared.system.RemoteAnimationTargetCompat;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

/**
 * Utility class which abstracts out the logical differences between Launcher and RecentsActivity.
 */
@TargetApi(Build.VERSION_CODES.P)
public interface ActivityControlHelper<T extends BaseDraggingActivity> {

    void onTransitionCancelled(T activity, boolean activityVisible);

    int getSwipeUpDestinationAndLength(DeviceProfile dp, Context context, Rect outRect);

    void onSwipeUpComplete(T activity);

    @NonNull HomeAnimationFactory prepareHomeUI(T activity);

    AnimationFactory prepareRecentsUI(T activity, boolean activityVisible,
            boolean animateActivity, Consumer<AnimatorPlaybackController> callback);

    ActivityInitListener createActivityInitListener(BiPredicate<T, Boolean> onInitListener);

    @Nullable
    T getCreatedActivity();

    default boolean isResumed() {
        BaseDraggingActivity activity = getCreatedActivity();
        return activity != null && activity.hasBeenResumed();
    }

    @UiThread
    @Nullable
    <T extends View> T getVisibleRecentsView();

    @UiThread
    boolean switchToRecentsIfVisible(Runnable onCompleteCallback);

    Rect getOverviewWindowBounds(Rect homeBounds, RemoteAnimationTargetCompat target);

    boolean shouldMinimizeSplitScreen();

    default boolean deferStartingActivity(Region activeNavBarRegion, MotionEvent ev) {
        return true;
    }

    AlphaProperty getAlphaProperty(T activity);

    /**
     * Used for containerType in {@link com.android.launcher3.logging.UserEventDispatcher}
     */
    int getContainerType();

    boolean isInLiveTileMode();

    interface ActivityInitListener {

        void register();

        void unregister();

        void registerAndStartActivity(Intent intent, RemoteAnimationProvider animProvider,
                Context context, Handler handler, long duration);
    }

    interface AnimationFactory {

        enum ShelfAnimState {
            HIDE(true), PEEK(true), OVERVIEW(false), CANCEL(false);

            ShelfAnimState(boolean shouldPreformHaptic) {
                this.shouldPreformHaptic = shouldPreformHaptic;
            }

            public final boolean shouldPreformHaptic;
        }

        default void onRemoteAnimationReceived(RemoteAnimationTargetSet targets) { }

        void createActivityController(long transitionLength);

        default void onTransitionCancelled() { }

        default void setShelfState(ShelfAnimState animState, Interpolator interpolator,
                long duration) { }
    }

    interface HomeAnimationFactory {

        /** Return the floating view that will animate in sync with the closing window. */
        default @Nullable View getFloatingView() {
            return null;
        }

        @NonNull RectF getWindowTargetRect();

        @NonNull Animator createActivityAnimationToHome();
    }
}

package com.viethoa;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viethoa.adapters.AlphabetAdapter;
import com.viethoa.models.AlphabetItem;

import java.util.List;

/**
 * Created by VietHoa on 07/10/15.
 */
public class RecyclerViewFastScroller extends LinearLayout {
    private static final int BUBBLE_ANIMATION_DURATION = 100;
    private static final int TRACK_SNAP_RANGE = 5;

    private TextView bubble;
    private View handle;
    private RecyclerView recyclerView;
    private int height;
    private boolean isInitialized = false;
    private ObjectAnimator currentAnimator = null;

    public interface BubbleTextGetter {
        String getTextToShowInBubble(int pos);
    }

    public RecyclerViewFastScroller(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialiseView();
    }

    public RecyclerViewFastScroller(final Context context) {
        super(context);
        initialiseView();
    }

    public RecyclerViewFastScroller(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initialiseView();
    }

    protected void initialiseView() {
        if (isInitialized)
            return;

        isInitialized = true;
        setOrientation(HORIZONTAL);
        setClipChildren(false);

        setViewsToUse(R.layout.fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle, R.id.alphabet);
    }

    private void setViewsToUse(@LayoutRes int layoutResId, @IdRes int bubbleResId, @IdRes int handleResId, @IdRes int alphabetListView) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(layoutResId, this, true);
        bubble = (TextView) findViewById(bubbleResId);
        //bubble.setVisibility(INVISIBLE);
        handle = findViewById(handleResId);

        //Alphabet
        alphabetRecyclerView = (RecyclerView) findViewById(alphabetListView);
        alphabetRecyclerView.setOnTouchListener(new AlphabetTouchListener());
        alphabetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < handle.getX() - ViewCompat.getPaddingStart(handle))
                    return false;
                if (currentAnimator != null)
                    currentAnimator.cancel();
                if (bubble != null && bubble.getVisibility() == INVISIBLE)
                    showBubble();
                handle.setSelected(true);
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                setBubbleAndHandlePosition(y);
                setRecyclerViewPosition(y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handle.setSelected(false);
                hideBubble();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                if (bubble == null || handle.isSelected() || recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE)
                    return;
                final int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
                final int verticalScrollRange = recyclerView.computeVerticalScrollRange();
                float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
                setBubbleAndHandlePosition(height * proportion);
                setRecyclerViewPositionWithoutScrolling(height * proportion);
                //bubble.setVisibility(VISIBLE);
                bubble.setAlpha(1f);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //bubble.setVisibility(INVISIBLE);
                    //clearAllAlphabetWorkSelected();
                }
            }
        };
        recyclerView.addOnScrollListener(onScrollListener);
    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            final int itemCount = recyclerView.getAdapter().getItemCount();
            float proportion;
            if (handle.getY() == 0)
                proportion = 0f;
            else if (handle.getY() + handle.getHeight() >= height - TRACK_SNAP_RANGE)
                proportion = 1f;
            else
                proportion = y / (float) height;
            final int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, 0);
            final String bubbleText = ((BubbleTextGetter) recyclerView.getAdapter()).getTextToShowInBubble(targetPos);
            if (bubble != null)
                bubble.setText(bubbleText);
            setAlphabetWorkSelected(bubbleText);
        }
    }

    private void setRecyclerViewPositionWithoutScrolling(float y) {
        if (recyclerView != null) {
            final int itemCount = recyclerView.getAdapter().getItemCount();
            float proportion;
            if (handle.getY() == 0)
                proportion = 0f;
            else if (handle.getY() + handle.getHeight() >= height - TRACK_SNAP_RANGE)
                proportion = 1f;
            else
                proportion = y / (float) height;
            final int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            final String bubbleText = ((BubbleTextGetter) recyclerView.getAdapter()).getTextToShowInBubble(targetPos);
            if (bubble != null)
                bubble.setText(bubbleText);
            setAlphabetWorkSelected(bubbleText);
        }
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    private void setBubbleAndHandlePosition(float y) {
        final int handleHeight = handle.getHeight();
        handle.setY(getValueInRange(getPaddingTop(), height - handleHeight, (int) (y - handleHeight / 2)));
        if (bubble != null) {
            int bubbleHeight = bubble.getHeight();
            bubble.setY(getValueInRange(getPaddingTop(), height - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
        }
    }

    private void showBubble() {
        if (bubble == null)
            return;
        //bubble.setVisibility(VISIBLE);
        if (currentAnimator != null)
            currentAnimator.cancel();
        currentAnimator = ObjectAnimator.ofFloat(bubble, "alpha", 0f, 1f).setDuration(BUBBLE_ANIMATION_DURATION);
        currentAnimator.start();
    }

    private void hideBubble() {
        if (bubble == null)
            return;
        if (currentAnimator != null)
            currentAnimator.cancel();
        currentAnimator = ObjectAnimator.ofFloat(bubble, "alpha", 1f, 0f).setDuration(BUBBLE_ANIMATION_DURATION);
        currentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //bubble.setVisibility(INVISIBLE);
                //clearAllAlphabetWorkSelected();
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                //bubble.setVisibility(INVISIBLE);
                //clearAllAlphabetWorkSelected();
                currentAnimator = null;
            }
        });
        currentAnimator.start();
    }

    //----------------------------------------------------------------------------------------------
    //  Alphabet Section
    //----------------------------------------------------------------------------------------------

    private List<AlphabetItem> alphabets;
    private RecyclerView alphabetRecyclerView;
    private AlphabetAdapter alphabetAdapter;

    public void setUpAlphabet(List<AlphabetItem> alphabetItems) {
        if (alphabetItems == null || alphabetItems.size() <= 0)
            return;

        alphabets = alphabetItems;
        alphabetAdapter = new AlphabetAdapter(getContext(), alphabets);
        alphabetAdapter.setOnItemClickListener(new OnAlphabetItemClickListener());
        alphabetRecyclerView.setAdapter(alphabetAdapter);
    }

    private class AlphabetTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE: {

                    Rect rect = new Rect();
                    int childCount = alphabetRecyclerView.getChildCount();
                    int[] listViewCoords = new int[2];
                    alphabetRecyclerView.getLocationOnScreen(listViewCoords);
                    int x = (int) motionEvent.getRawX() - listViewCoords[0];
                    int y = (int) motionEvent.getRawY() - listViewCoords[1];

                    View child;
                    for (int i = 0; i < childCount; i++) {
                        child = alphabetRecyclerView.getChildAt(i);
                        child.getHitRect(rect);

                        // This is your pressed view
                        if (rect.contains(x, y)) {
                            LinearLayoutManager layoutManager = ((LinearLayoutManager)alphabetRecyclerView.getLayoutManager());
                            int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                            int position = i + firstVisiblePosition;
                            performSelectedAlphabetWord(position);
                            alphabetTouchEventOnItem(position);
                            break;
                        }
                    }
                    view.onTouchEvent(motionEvent);
                }
            }
            return true;
        }
    }

    private void performSelectedAlphabetWord(int position) {
        if (position < 0 || position >= alphabets.size())
            return;

        for (AlphabetItem alphabetItem : alphabets) {
            alphabetItem.isActive = false;
        }

        alphabets.get(position).isActive = true;
        alphabetAdapter.refreshDataChange(alphabets);
    }

    private void alphabetTouchEventOnItem(int position) {
        if (alphabets == null || position < 0 || position >= alphabets.size())
            return;

        takeRecyclerViewScrollToAlphabetPosition(alphabets.get(position).position);
    }

    private class OnAlphabetItemClickListener implements AlphabetAdapter.OnItemClickListener {
        @Override
        public void OnItemClicked(int alphabetPosition, int position) {
            performSelectedAlphabetWord(position);
            takeRecyclerViewScrollToAlphabetPosition(alphabetPosition);
        }
    }

    private void takeRecyclerViewScrollToAlphabetPosition(int position) {
        if (recyclerView == null || recyclerView.getAdapter() == null)
            return;

        int count = recyclerView.getAdapter().getItemCount();
        if (position < 0 || position > count)
            return;

        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    private void setAlphabetWorkSelected(String bubbleText) {
        if (bubbleText == null || bubbleText.trim().isEmpty())
            return;

        for (int i = 0; i < alphabets.size(); i++) {
            AlphabetItem alphabetItem = alphabets.get(i);
            if (alphabetItem == null || alphabetItem.word.trim().isEmpty())
                continue;

            if (alphabetItem.word.equals(bubbleText)) {
                performSelectedAlphabetWord(i);
                alphabetRecyclerView.smoothScrollToPosition(i);
                break;
            }
        }
    }



}

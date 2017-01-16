package com.viethoa;

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

import com.viethoa.adapters.AlphabetAdapter;
import com.viethoa.models.AlphabetItem;

import java.util.List;

/**
 * Created by VietHoa on 07/10/15.
 */
public class RecyclerViewFastScroller extends LinearLayout implements
        AlphabetAdapter.OnItemClickListener,
        View.OnTouchListener {

    private RecyclerView recyclerView;
    private List<AlphabetItem> alphabets;
    private RecyclerView alphabetRecyclerView;
    private AlphabetAdapter alphabetAdapter;
    private boolean isInitialized = false;
    private int height;

    public interface BubbleTextGetter {
        String getTextToShowInBubble(int pos);
    }

    public RecyclerViewFastScroller(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialiseView(context);
    }

    public RecyclerViewFastScroller(final Context context) {
        super(context);
        initialiseView(context);
    }

    public RecyclerViewFastScroller(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initialiseView(context);
    }

    protected void initialiseView(Context context) {
        if (isInitialized) {
            return;
        }

        // Init linear layout
        isInitialized = true;
        setOrientation(HORIZONTAL);
        setClipChildren(false);
        View.inflate(context, R.layout.fast_scroller, this);
        //final LayoutInflater inflater = LayoutInflater.from(getContext());
        //inflater.inflate(R.layout.fast_scroller, this, true);

        // Init alphabet recycler view
        alphabetRecyclerView = (RecyclerView) findViewById(R.id.alphabet);
        alphabetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alphabetRecyclerView.setOnTouchListener(this);
    }

    //----------------------------------------------------------------------------------------------
    //  Linear layout events
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                setRecyclerViewPosition(y);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            final int itemCount = recyclerView.getAdapter().getItemCount();
            final float proportion = y / (float) height;
            final int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, 0);

            final String bubbleText = ((BubbleTextGetter) recyclerView.getAdapter()).getTextToShowInBubble(targetPos);
            setAlphabetWordSelected(bubbleText);
        }
    }

    //----------------------------------------------------------------------------------------------
    //  Implement events
    //----------------------------------------------------------------------------------------------

    @Override
    public void OnItemClicked(int alphabetPosition, int position) {
        performSelectedAlphabetWord(position);
        takeRecyclerViewScrollToAlphabetPosition(alphabetPosition);
    }

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

    //----------------------------------------------------------------------------------------------
    //  Alphabet Section
    //----------------------------------------------------------------------------------------------

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }
                final int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
                final int verticalScrollRange = recyclerView.computeVerticalScrollRange();
                final float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
                setRecyclerViewPositionWithoutScrolling(height * proportion);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                }
            }
        };
        this.recyclerView.addOnScrollListener(onScrollListener);
    }

    public void setUpAlphabet(List<AlphabetItem> alphabetItems) {
        if (alphabetItems == null || alphabetItems.size() <= 0)
            return;

        alphabets = alphabetItems;
        alphabetAdapter = new AlphabetAdapter(getContext(), alphabets);
        alphabetAdapter.setOnItemClickListener(this);
        alphabetRecyclerView.setAdapter(alphabetAdapter);
    }

    private void setRecyclerViewPositionWithoutScrolling(float y) {
        if (recyclerView != null) {
            final int itemCount = recyclerView.getAdapter().getItemCount();
            final float proportion = y / (float) height;
            final int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            final String bubbleText = ((BubbleTextGetter) recyclerView.getAdapter()).getTextToShowInBubble(targetPos);
            setAlphabetWordSelected(bubbleText);
        }
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    private void performSelectedAlphabetWord(int position) {
        if (position < 0 || position >= alphabets.size()) {
            return;
        }

        for (AlphabetItem alphabetItem : alphabets) {
            alphabetItem.isActive = false;
        }

        alphabets.get(position).isActive = true;
        alphabetAdapter.refreshDataChange(alphabets);
    }

    private void alphabetTouchEventOnItem(int position) {
        if (alphabets == null || position < 0 || position >= alphabets.size()) {
            return;
        }

        takeRecyclerViewScrollToAlphabetPosition(alphabets.get(position).position);
    }

    private void takeRecyclerViewScrollToAlphabetPosition(int position) {
        if (recyclerView == null || recyclerView.getAdapter() == null) {
            return;
        }

        int count = recyclerView.getAdapter().getItemCount();
        if (position < 0 || position > count) {
            return;
        }

        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    private void setAlphabetWordSelected(String bubbleText) {
        if (bubbleText == null || bubbleText.trim().isEmpty()) {
            return;
        }

        for (int i = 0; i < alphabets.size(); i++) {
            AlphabetItem alphabetItem = alphabets.get(i);
            if (alphabetItem == null || alphabetItem.word.trim().isEmpty()) {
                continue;
            }

            if (alphabetItem.word.equals(bubbleText)) {
                performSelectedAlphabetWord(i);
                alphabetRecyclerView.smoothScrollToPosition(i);
                break;
            }
        }
    }
}

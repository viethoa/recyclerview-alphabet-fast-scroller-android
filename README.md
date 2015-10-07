Fast scroller alphabet
-----
This is an Open Source Android library that allows developers to easily add fast scroller alphabet to recycler view.
Feel free to use it all you want in your Android apps provided that you cite this project and include the license in your app.
Support Sdk Version 14 to lastest.

Feature
-----
- Auto dismiss dialog in Negative or Positive button clicked.
- Simple to set cancelable.
- Set `null` for optional that you don't to use this.

![](https://github.com/viethoa/image-repositories/blob/master/fastcsrcolleralphabet.gif "Fast scroller alphabet")

Dependency
-----
    repositories {
        maven { url 'https://oss.sonatype.org/content/groups/public' }
    }

    dependencies {
        compile 'com.github.viethoa:fastscroller:1.0.0-SNAPSHOT'
    }

    **Or**:

    Download this project and import library in there.

How to use
-----
    **Step 1:** add this to your xml.

        <com.viethoa.RecyclerViewFastScroller
                android:id="@+id/fast_scroller"
                android:layout_width="wrap_content"
                android:layout_height="match_parent"
                android:layout_alignParentEnd="true"
                android:layout_alignParentRight="true"/>

    **Step 2:** set recycler view for RecyclerViewFastscroller.

        fastScroller.setRecyclerView(mRecyclerView);

    **Step 3:** generate alphabet item.

        ArrayList<AlphabetItem> mAlphabetItems = new ArrayList<>();
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i < mDataArray.size(); i++) {
            String name = mDataArray.get(i);
            if (name == null || name.trim().isEmpty())
                continue;

            String word = name.substring(0, 1);
            if (!strAlphabets.contains(word)) {
                strAlphabets.add(word);
                mAlphabetItems.add(new AlphabetItem(i, word, false));
            }
        }

        fastScroller.setUpAlphabet(mAlphabetItems);

        Note: mDataArray: this is your recycler data array model, just get the name you want to fast scroll.

    **Step 4:** implement bubble text getter listener in your RecyclerViewAdapter.

        implements RecyclerViewFastScroller.BubbleTextGetter

        @Override
        public String getTextToShowInBubble(int position) {
            if (pos < 0 || pos >= mDataArray.size())
                return null;

            String name = mDataArray.get(pos);
            if (name == null || name.length() < 1)
                return null;

            return mDataArray.get(pos).substring(0, 1);
        }

        Note: This function to mapping your RecyclerView with FastScroller alphabet in scrolling.






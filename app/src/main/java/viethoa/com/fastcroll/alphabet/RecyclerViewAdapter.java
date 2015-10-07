package viethoa.com.fastcroll.alphabet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viethoa.RecyclerViewFastScroller;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by VietHoa on 07/10/15.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
    implements RecyclerViewFastScroller.BubbleTextGetter {

    private List<String> mDataArray;

    public RecyclerViewAdapter(List<String> dataset) {
        mDataArray = dataset;
    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mDataArray.get(position));
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        if (pos < 0 || pos >= mDataArray.size())
            return null;

        String name = mDataArray.get(pos);
        if (name == null || name.length() < 1)
            return null;

        return mDataArray.get(pos).substring(0, 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_alphabet)
        TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

package viethoa.com.fastcroll.alphabet.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import viethoa.com.fastcroll.alphabet.R;
import viethoa.com.fastcroll.alphabet.models.AlphabetItem;

/**
 * Created by VietHoa on 02/10/15.
 */
public class AlphabetAdapter extends RecyclerView.Adapter<AlphabetAdapter.ViewHolder> {
    private Context mContext;
    private List<AlphabetItem> mDataArray;
    private OnItemClickListener listener;

    public AlphabetAdapter(Context context, List<AlphabetItem> dataSet) {
        this.mContext = context;
        this.mDataArray = dataSet;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void refreshDataChange(List<AlphabetItem> newDataSet) {
        this.mDataArray = newDataSet;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View alphabet = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alphabet_layout, parent, false);
        return new ViewHolder(alphabet);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mDataArray.get(position));
    }

    public interface OnItemClickListener {
        void OnItemClicked(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_word)
        TextView tvWord;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void bind(final AlphabetItem alphabetItem) {
            if (alphabetItem == null || alphabetItem.word == null)
                return;

            tvWord.setText(alphabetItem.word);
            tvWord.setTypeface(null, alphabetItem.isActive ? Typeface.BOLD : Typeface.NORMAL);
            tvWord.setTextColor(alphabetItem.isActive ?
                    mContext.getResources().getColor(R.color.primary_color) :
                    mContext.getResources().getColor(R.color.dark_color));

            tvWord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener == null)
                        return;
                    listener.OnItemClicked(alphabetItem.position);
                }
            });
        }
    }
}

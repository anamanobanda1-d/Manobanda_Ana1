package fisei.uta.ec.bookapp;

import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import fisei.uta.ec.bookapp.data.DatabaseDescription.Contact;


public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {


    public interface ContactClickListener {
        void onClick(Uri contactUri);
    }


    private Cursor cursor = null;
    private final ContactClickListener clickListener;

    // Constructor
    public ContactsAdapter(ContactClickListener listener) {
        clickListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        long rowID = cursor.getLong(cursor.getColumnIndexOrThrow(Contact._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(Contact.COLUMN_NAME));

        holder.textView.setText(name);
        holder.setRowID(rowID);
    }


    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null)
            cursor.close();

        cursor = newCursor;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;
        private long rowID;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onClick(Contact.buildContactUri(rowID));
                }
            });
        }

        public void setRowID(long rowID) {
            this.rowID = rowID;
        }
    }
}

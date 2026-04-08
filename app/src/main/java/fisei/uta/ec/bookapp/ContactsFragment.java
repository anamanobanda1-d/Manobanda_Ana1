package fisei.uta.ec.bookapp;

import android.content.ContentValues;
import android.content.Context;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import fisei.uta.ec.bookapp.data.DatabaseDescription;

public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public interface ContactsFragmentListener{
        void onContactSelected(Uri contactUri);

        void onAddContact();

    }

    // Método para eliminar un contacto
    private void deleteContact(Uri contactUri) {
        // Guardar el contacto eliminado en un Cursor temporal
        Cursor cursor = getActivity().getContentResolver().query(
                contactUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Extraer datos del contacto eliminado
            final String deletedName = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseDescription.Contact.COLUMN_NAME));
            cursor.close();

            // Eliminar de la BD
            int deletedRows = getActivity().getContentResolver().delete(contactUri, null, null);

            if (deletedRows > 0) {
                // Refrescar lista
                LoaderManager.getInstance(requireActivity()).restartLoader(CONTACTS_LOADER, null, this);

                // Mostrar Snackbar con opción DESHACER
                Snackbar snackbar = Snackbar.make(getView(), "Contacto eliminado", Snackbar.LENGTH_LONG);
                snackbar.setAction("DESHACER", v -> {
                    // Restaurar el contacto eliminado (ejemplo: solo nombre, puedes añadir más campos)
                    ContentValues values = new ContentValues();
                    values.put(DatabaseDescription.Contact.COLUMN_NAME, deletedName);
                    getActivity().getContentResolver().insert(DatabaseDescription.Contact.CONTENT_URI, values);

                    // Refrescar lista otra vez
                    LoaderManager.getInstance(requireActivity()).restartLoader(CONTACTS_LOADER, null, this);
                });
                snackbar.show();
            }
        }
    }


    private static final int CONTACTS_LOADER = 0;

    private ContactsFragmentListener listener;
    private ContactsAdapter contactsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(
                R.layout.fragment_contacts, container, false
        );

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));
        contactsAdapter = new ContactsAdapter(
                new ContactsAdapter.ContactClickListener(){
                    @Override
                    public void onClick(Uri contactUri){
                        listener.onContactSelected(contactUri);
                    }
                }
        );

        recyclerView.setAdapter(contactsAdapter);
        recyclerView.addItemDecoration(new ItemDivider(getContext()));
        recyclerView.setHasFixedSize(true);

        FloatingActionButton addButton = (FloatingActionButton) view.findViewById(R.id.addButton);
        addButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        listener.onAddContact();
                    }
                }
        );
        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        listener = (ContactsFragmentListener) context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        listener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        LoaderManager.getInstance(requireActivity()).initLoader(CONTACTS_LOADER, null, this);


    }

    public void updateContractList(){
        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        switch (id){
            case CONTACTS_LOADER:
                return new CursorLoader(getActivity(),
                        DatabaseDescription.Contact.CONTENT_URI,
                        null,
                        null,
                        null,
                        DatabaseDescription.Contact.COLUMN_NAME + " COLLATE NOCASE ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        contactsAdapter.swapCursor(data);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        contactsAdapter.swapCursor(null);
    }

}
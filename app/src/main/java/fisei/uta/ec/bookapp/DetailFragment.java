package fisei.uta.ec.bookapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import androidx.loader.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.snackbar.Snackbar;

import fisei.uta.ec.bookapp.data.DatabaseDescription;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface DetailFragmentListener {
        void onContactDeleted();
        void onEditContact(Uri contactUri);
        void onAddEditComplete(Uri contactUri); // necesario para refrescar al restaurar
    }

    private static final int CONTACT_LOADER = 0;

    private DetailFragmentListener listener;
    private Uri contactUri;

    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView emailTextView;
    private TextView streetTextView;
    private TextView cityTextView;
    private TextView stateTextView;
    private TextView zipTextView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DetailFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        super.onCreateView(inflater, container, savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null)
            contactUri = arguments.getParcelable(MainActivity.CONTACT_URI);

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        nameTextView = view.findViewById(R.id.nameTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        streetTextView = view.findViewById(R.id.streetTextView);
        cityTextView = view.findViewById(R.id.cityTextView);
        stateTextView = view.findViewById(R.id.stateTextView);
        zipTextView = view.findViewById(R.id.zipTextView);

        LoaderManager.getInstance(this).initLoader(CONTACT_LOADER, null, this);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_details_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.action_edit) {
                    listener.onEditContact(contactUri);
                    return true;
                } else if (id == R.id.action_delete) {
                    deleteContact();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return view;
    }

    public static class ConfirmDeleteDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);

            builder.setPositiveButton(R.string.button_delete,
                    (dialog, button) -> {
                        Fragment parentFragment = getParentFragment();
                        if (parentFragment instanceof DetailFragment) {
                            DetailFragment detailFragment = (DetailFragment) parentFragment;
                            if (detailFragment.getActivity() != null) {
                                detailFragment.deleteContact(); // reutiliza la lógica con Snackbar
                            }
                        }
                    });

            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create();
        }
    }

    private void deleteContact() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_title)
                .setMessage(R.string.confirm_message)
                .setPositiveButton(R.string.button_delete, (dialog, which) -> {
                    if (contactUri != null) {
                        // Guardar datos del contacto antes de eliminar
                        Cursor cursor = requireActivity().getContentResolver().query(
                                contactUri, null, null, null, null);

                        if (cursor != null && cursor.moveToFirst()) {
                            final ContentValues deletedValues = new ContentValues();
                            deletedValues.put(DatabaseDescription.Contact.COLUMN_NAME,
                                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDescription.Contact.COLUMN_NAME)));
                            deletedValues.put(DatabaseDescription.Contact.COLUMN_PHONE,
                                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDescription.Contact.COLUMN_PHONE)));
                            deletedValues.put(DatabaseDescription.Contact.COLUMN_EMAIL,
                                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDescription.Contact.COLUMN_EMAIL)));
                            deletedValues.put(DatabaseDescription.Contact.COLUMN_STREET,
                                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDescription.Contact.COLUMN_STREET)));
                            deletedValues.put(DatabaseDescription.Contact.COLUMN_CITY,
                                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDescription.Contact.COLUMN_CITY)));
                            deletedValues.put(DatabaseDescription.Contact.COLUMN_STATE,
                                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDescription.Contact.COLUMN_STATE)));
                            deletedValues.put(DatabaseDescription.Contact.COLUMN_ZIP,
                                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDescription.Contact.COLUMN_ZIP)));
                            cursor.close();

                            // Eliminar contacto
                            int rowsDeleted = requireActivity().getContentResolver().delete(contactUri, null, null);
                            if (rowsDeleted > 0 && listener != null) {
                                listener.onContactDeleted();

                                // Mostrar Snackbar con opción DESHACER
                                Snackbar snackbar = Snackbar.make(requireView(), "Contacto eliminado", Snackbar.LENGTH_LONG);
                                snackbar.setAction("DESHACER", v -> {
                                    Uri restoredUri = requireActivity().getContentResolver().insert(
                                            DatabaseDescription.Contact.CONTENT_URI, deletedValues);

                                    if (restoredUri != null) {
                                        listener.onAddEditComplete(restoredUri); // refresca lista y muestra contacto restaurado
                                        Snackbar.make(requireView(), "Contacto restaurado", Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                                snackbar.show();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CONTACT_LOADER) {
            return new CursorLoader(getActivity(),
                    contactUri,
                    null,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            int nameIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_NAME);
            int phoneIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_PHONE);
            int emailIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_EMAIL);
            int streetIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_STREET);
            int cityIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_CITY);
            int stateIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_STATE);
            int zipIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_ZIP);

            nameTextView.setText(data.getString(nameIndex));
            phoneTextView.setText(data.getString(phoneIndex));
            emailTextView.setText(data.getString(emailIndex));
            streetTextView.setText(data.getString(streetIndex));
            cityTextView.setText(data.getString(cityIndex));
            stateTextView.setText(data.getString(stateIndex));
            zipTextView.setText(data.getString(zipIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // No action needed
    }
}

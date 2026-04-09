package fisei.uta.ec.bookapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import fisei.uta.ec.bookapp.data.DatabaseDescription;

public class AddEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface AddEditFragmentListener{
        void onAddEditComplete(Uri contactUri);
    }

    private static final int CONTACT_LOADER = 0;
    private AddEditFragmentListener listener;
    private Uri contactUri;
    private boolean addingNewContact = true;

    private TextInputLayout nameTextInputLayout;
    private TextInputLayout phoneTextInputLayout;
    private TextInputLayout emailTextInputLayout;
    private TextInputLayout streetTextInputLayout;
    private TextInputLayout cityTextInputLayout;
    private TextInputLayout stateTextInputLayout;
    private TextInputLayout zipTextInputLayout;
    private FloatingActionButton saveContactFAB;

    private CoordinatorLayout coordinatorLayout;

    private String originalName, originalPhone, originalEmail, originalStreet, originalCity, originalState, originalZip;

    private boolean hasUnsavedChanges() {
        String currentName = nameTextInputLayout.getEditText().getText().toString();
        String currentPhone = phoneTextInputLayout.getEditText().getText().toString();
        String currentEmail = emailTextInputLayout.getEditText().getText().toString();
        String currentStreet = streetTextInputLayout.getEditText().getText().toString();
        String currentCity = cityTextInputLayout.getEditText().getText().toString();
        String currentState = stateTextInputLayout.getEditText().getText().toString();
        String currentZip = zipTextInputLayout.getEditText().getText().toString();

        return !(currentName.equals(originalName) &&
                currentPhone.equals(originalPhone) &&
                currentEmail.equals(originalEmail) &&
                currentStreet.equals(originalStreet) &&
                currentCity.equals(originalCity) &&
                currentState.equals(originalState) &&
                currentZip.equals(originalZip));
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        listener = (AddEditFragmentListener) context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ){

        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);
        coordinatorLayout = view.findViewById(R.id.coordinator_layout);


        nameTextInputLayout = (TextInputLayout) view. findViewById(R.id.nameTextInputLayout);

        nameTextInputLayout. getEditText().addTextChangedListener(nameChangedListener);

        phoneTextInputLayout = (TextInputLayout) view.findViewById(R.id.phoneTextInputLayout);

        emailTextInputLayout = (TextInputLayout) view.findViewById(R.id.emailTextInputLayout);

        streetTextInputLayout = (TextInputLayout) view.findViewById(R.id.streetTextInputLayout);

        cityTextInputLayout = (TextInputLayout) view.findViewById(R.id.cityTextInputLayout);

        stateTextInputLayout = (TextInputLayout) view.findViewById(R.id.stateTextInputLayout);

        zipTextInputLayout = (TextInputLayout) view.findViewById(R.id.zipTextInputLayout);


        saveContactFAB = (FloatingActionButton) view.findViewById(R.id.saveFloatingActionButton);
        saveContactFAB.setOnClickListener(saveContactButtonClicked);
        updateSaveButtonFAB();

        Bundle arguments = getArguments();

        if (arguments != null){
            addingNewContact = false;
            contactUri = arguments.getParcelable(MainActivity.CONTACT_URI);

        }

        if (contactUri != null)
            LoaderManager.getInstance(this).initLoader(CONTACT_LOADER, null, this);
        return  view;
    }

    private final TextWatcher nameChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSaveButtonFAB();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    private void updateSaveButtonFAB(){
        String input = nameTextInputLayout.getEditText().getText().toString();

        if (input.trim().length() != 0)
            saveContactFAB.show();
        else
            saveContactFAB.hide();
    }

    private final View.OnClickListener saveContactButtonClicked =
            new View.OnClickListener(){
        @Override
                public void onClick(View v){
            ((InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE
            )).hideSoftInputFromWindow(
                    getView().getWindowToken(), 0
            );
            saveContact();
        }
    };

    private void saveContact(){
        ContentValues contentValues =  new ContentValues();
        originalName = nameTextInputLayout.getEditText().getText().toString();
        originalPhone = phoneTextInputLayout.getEditText().getText().toString();
        originalEmail = emailTextInputLayout.getEditText().getText().toString();
        originalStreet = streetTextInputLayout.getEditText().getText().toString();
        originalCity = cityTextInputLayout.getEditText().getText().toString();
        originalState = stateTextInputLayout.getEditText().getText().toString();
        originalZip = zipTextInputLayout.getEditText().getText().toString();

        // Obtener valores de los campos
        String name = nameTextInputLayout.getEditText().getText().toString().trim();
        String phone = phoneTextInputLayout.getEditText().getText().toString().trim();
        String email = emailTextInputLayout.getEditText().getText().toString().trim();
        String zip = zipTextInputLayout.getEditText().getText().toString().trim();
        //Fase 2 - Normalización del teléfono
        String phoneRaw = phoneTextInputLayout.getEditText().getText().toString().trim();
        phone = phoneRaw.replaceAll("[\\s-]", ""); // elimina espacios y guiones


// Validación: Nombre obligatorio
        if (name.isEmpty()) {
            Snackbar.make(coordinatorLayout, "El nombre es obligatorio", Snackbar.LENGTH_LONG).show();
            return;
        }

// Validación: Email opcional con formato válido
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Snackbar.make(coordinatorLayout, "Email inválido", Snackbar.LENGTH_LONG).show();
            return;
        }

// Validación: Teléfono de 10 dígitos

        if (!phone.matches("\\d{10}")) {
            Snackbar.make(coordinatorLayout, "Teléfono debe tener exactamente 10 dígitos", Snackbar.LENGTH_LONG).show();
            return;
        }

// Validación: ZIP de 5 dígitos
        if (!zip.matches("\\d{5}")) {
            Snackbar.make(coordinatorLayout, "ZIP debe tener exactamente 5 dígitos", Snackbar.LENGTH_LONG).show();
            return;
        }

        contentValues.put(DatabaseDescription.Contact.COLUMN_NAME,
                nameTextInputLayout.getEditText().getText().toString());

        contentValues.put(DatabaseDescription.Contact.COLUMN_PHONE, phone);

        contentValues.put(DatabaseDescription.Contact.COLUMN_PHONE,
                phoneTextInputLayout.getEditText().getText().toString());

        contentValues.put(DatabaseDescription.Contact.COLUMN_EMAIL,
                emailTextInputLayout.getEditText().getText().toString());

        contentValues.put(DatabaseDescription.Contact.COLUMN_STREET,
                streetTextInputLayout.getEditText().getText().toString());

        contentValues.put(DatabaseDescription.Contact.COLUMN_CITY,
                cityTextInputLayout.getEditText().getText().toString());

        contentValues.put(DatabaseDescription.Contact.COLUMN_STATE,
                stateTextInputLayout.getEditText().getText().toString());

        contentValues.put(DatabaseDescription.Contact.COLUMN_ZIP,
                zipTextInputLayout.getEditText().getText().toString());

        if (addingNewContact){
            Uri newContactUri = getActivity().getContentResolver().insert(
                    DatabaseDescription.Contact.CONTENT_URI, contentValues
            );

            if(newContactUri != null){

                Snackbar.make(coordinatorLayout, R.string.contact_added, Snackbar.LENGTH_LONG).show();
                listener.onAddEditComplete(newContactUri);
            }
            else {
                Snackbar.make(coordinatorLayout, R.string.contact_not_added, Snackbar.LENGTH_LONG).show();
            }
        }
        else {
            int updateRows = getActivity().getContentResolver().update(
                    contactUri, contentValues, null, null
            );
            if (updateRows >0){
                listener.onAddEditComplete(contactUri);
                Snackbar.make(coordinatorLayout,
                        R.string.contact_updated, Snackbar.LENGTH_LONG).show();
            }
            else{
                Snackbar.make(coordinatorLayout,
                        R.string.contact_not_updated, Snackbar.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        switch (id){
            case CONTACT_LOADER:
                return  new CursorLoader(getActivity(), contactUri, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        if (data != null && data.moveToFirst()){
            int nameIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_NAME);
            int phoneIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_PHONE);
            int emailIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_EMAIL);
            int streetIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_STREET);
            int cityIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_CITY);
            int stateIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_STATE);
            int zipIndex = data.getColumnIndex(DatabaseDescription.Contact.COLUMN_ZIP);

            nameTextInputLayout.getEditText().setText(
                    data.getString(nameIndex)
            );
            phoneTextInputLayout.getEditText().setText(
                    data.getString(phoneIndex)
            );
            emailTextInputLayout.getEditText().setText(
                    data.getString(emailIndex)
            );
            streetTextInputLayout.getEditText().setText(
                    data.getString(streetIndex)
            );
            cityTextInputLayout.getEditText().setText(
                    data.getString(cityIndex)
            );
            stateTextInputLayout.getEditText().setText(
                    data.getString(stateIndex)
            );
            zipTextInputLayout.getEditText().setText(
                    data.getString(zipIndex)
            );

            updateSaveButtonFAB();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){}

    @Override
    public void onPause() {
        super.onPause();
        if (hasUnsavedChanges()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cambios no guardados")
                    .setMessage("¿Desea salir sin guardar o continuar editando?")
                    .setPositiveButton("Salir sin guardar", (dialog, which) -> {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .setNegativeButton("Continuar editando", null)
                    .show();

        }
    }



}

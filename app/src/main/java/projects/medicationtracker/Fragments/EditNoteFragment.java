package projects.medicationtracker.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.MedicationNotes;
import projects.medicationtracker.SimpleClasses.Note;
import projects.medicationtracker.R;

public class EditNoteFragment extends DialogFragment
{
    final DBHelper db;
    final Note note;

    public EditNoteFragment(Note note, DBHelper db)
    {
        this.note = note;
        this.db = db;
    }

    /**
     * Creates a DialogFragment allowing the user to change a note
     * @param savedInstanceState Saved instances
     * @return The Created Dialog
     **************************************************************************/
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.fragment_edit_note, null));
        builder.setTitle("Edit Note");

        builder.setPositiveButton("OK", ((dialogInterface, i) ->
        {
            EditText alterNote = getDialog().findViewById(R.id.alterNote);
            db.updateNote(note, alterNote.getText().toString());
            restartActivity();
        }));

        builder.setNegativeButton("Cancel", ((dialogInterface, i) -> dismiss()));

        builder.setNeutralButton("Delete", ((dialogInterface, i) ->
        {
            db.deleteNote(note);
            restartActivity();
        }));

        return builder.create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog)
    {
        super.onCancel(dialog);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        EditText alterNote = getDialog().findViewById(R.id.alterNote);
        alterNote.setText(note.getNote());
    }

    public void restartActivity()
    {
        Intent intent = new Intent(getContext(), MedicationNotes.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("medId", note.getMedId());
        startActivity(intent);
    }
}

package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDateTime;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.R;

public class DoseInfoDialog extends DialogFragment
{
    private final long doseId;
    private DBHelper db;

    public DoseInfoDialog(long doseId, DBHelper database)
    {
        this.doseId = doseId;
        db = database;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_dose_info, null));
        builder.setTitle(R.string.this_dose);

        builder.setPositiveButton(R.string.close, ((dialogInterface, i) -> dismiss()));

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

        TextView timeTaken = getDialog().findViewById(R.id.dose_time_taken);
        TextView dateTaken = getDialog().findViewById(R.id.dose_date_taken);
        LocalDateTime doseDate = db.getTimeTaken(doseId);

        timeTaken.setText(
                doseDate != null ? TimeFormatting.localTimeToString(doseDate.toLocalTime()) : getString(R.string.not_taken_yet)
        );

        dateTaken.setText(
                doseDate != null ? TimeFormatting.localDateToString(doseDate.toLocalDate()) : getString(R.string.not_taken_yet)
        );
    }
}

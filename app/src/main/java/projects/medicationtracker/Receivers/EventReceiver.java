package projects.medicationtracker.Receivers;

import static projects.medicationtracker.Helpers.NotificationHelper.MEDICATION_ID;
import static projects.medicationtracker.Helpers.NotificationHelper.NOTIFICATION_ID;
import static projects.medicationtracker.Services.NotificationService.SNOOZE_ACTION;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NotificationHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.Services.NotificationService;
import projects.medicationtracker.SimpleClasses.Medication;

public class EventReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        final DBHelper db = new DBHelper(context);
        ArrayList<Medication> medications = db.getMedications();

        if (intent.getAction().contains(NotificationService.MARK_AS_TAKEN_ACTION))
        {
            String medId = "_" + intent.getAction().split("_")[1];

            markDoseTaken(
                    context,
                    intent.getLongExtra(NOTIFICATION_ID + medId, 0),
                    intent.getLongExtra(MEDICATION_ID + medId, 0),
                    intent.getStringExtra(NotificationHelper.DOSE_TIME + medId),
                    db
            );

            return;
        }
        else if (intent.getAction().contains(SNOOZE_ACTION))
        {
            String medId = "_" + intent.getAction().split("_")[1];

            snoozeFor15(
                    context,
                    intent.getLongExtra(NOTIFICATION_ID + medId, 0),
                    intent.getLongExtra(MEDICATION_ID + medId, 0),
                    intent.getStringExtra(NotificationHelper.DOSE_TIME + medId),
                    db
            );

            return;
        }

        for (final Medication medication : medications)
        {
            prepareNotification(context, db, medication);
        }

        db.close();
    }

    /**
     * Prepares pending notifications on boot
     * @param context Notification context
     * @param db DBHelper containing medication data
     * @param medication Medication in need of a notification
     */
    private void prepareNotification(Context context, DBHelper db, Medication medication)
    {
        LocalTime[] times = db.getMedicationTimes(medication.getMedId());
        LocalDate startDate = medication.getStartDate().toLocalDate();
        long[] timeIds = db.getMedicationTimeIds(medication);

        for (int i = 0; i < times.length; i++)
        {
            long notificationId = times.length > 1 ? timeIds[i] * -1 : medication.getMedId();

            NotificationHelper.scheduleNotification(
                    context,
                    medication,
                    LocalDateTime.of(startDate, times[i]),
                    notificationId
            );
        }
    }

    /**
     * Marks a dose as taken from the notification
     * @param context Application context
     * @param notificationId Id of notification to cancel
     * @param medId ID of medication taken
     * @param doseTimeString Dose time for DB.
     */
    private void markDoseTaken(
            Context context, long notificationId, long medId, String doseTimeString, DBHelper db
    )
    {
        Medication med;
        LocalDateTime doseTime = LocalDateTime.parse(doseTimeString);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationId == 0 || medId == 0)
        {
            Toast.makeText(
                    context, "Medication could not be determined", Toast.LENGTH_LONG
            ).show();

            return;
        }

        med =  db.getMedication(medId);

        long doseId = db.isInMedicationTracker(med, doseTime) ?
                db.getDoseId(med.getMedId(), TimeFormatting.localDateTimeToString(doseTime)) :
                db.addToMedicationTracker(med, doseTime);

        db.updateDoseStatus(
                doseId, TimeFormatting.localDateTimeToString(LocalDateTime.now()), true
        );

        notificationManager.cancel((int) notificationId);
        db.close();
    }

    private void snoozeFor15(
            Context context, long notificationId, long medId, String doseTimeString, DBHelper db
    )
    {
        Medication med;
        LocalDateTime doseTime = LocalDateTime.parse(doseTimeString);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationId == 0 || medId == 0)
        {
            Toast.makeText(
                    context, "Medication could not be determined", Toast.LENGTH_LONG
            ).show();

            return;
        }

        med = db.getMedication(medId);

        NotificationHelper.scheduleIn15Minutes(
                context,
                med,
                doseTime,
                notificationId
        );

        notificationManager.cancel((int) notificationId);
        db.close();
    }
}

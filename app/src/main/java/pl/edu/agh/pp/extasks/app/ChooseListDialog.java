package pl.edu.agh.pp.extasks.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import org.trello4j.model.List;

import java.util.Map;
import java.util.TreeMap;

import pl.edu.agh.pp.extasks.framework.NoteList;


/**
 * Created by Maciek on 2014-10-12.
 */
public class ChooseListDialog extends DialogFragment {
    public Map<String, NoteList> itemsMap = new TreeMap<>();
    public LayoutInflater inflater;
    public CharSequence chosenList;
    public CharSequence chosenListID;
    private chooseListInt mListener;

    static ChooseListDialog newInstance(String[] items) {
        ChooseListDialog c = new ChooseListDialog();
        Bundle args = new Bundle();
        args.putStringArray("items", items);
        c.setArguments(args);

        return c;
    }

    public void appendItemsMap(Map<String, NoteList> items) {
        itemsMap.putAll(items);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (chooseListInt) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final CharSequence[] items = getArguments().getStringArray("items");
        inflater = getActivity().getLayoutInflater();

        builder.setTitle("Choose a list")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String chosen = items[which].toString();
                        mListener.onListChoose(chosen, itemsMap.get(chosen).getId());
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public interface chooseListInt {
        public void onListChoose(String ListName, String listID);
    }
}

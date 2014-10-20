package pl.edu.agh.pp.extasks.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import org.trello4j.model.List;

import java.util.Map;


/**
 * Created by Maciek on 2014-10-12.
 */
public class ChooseListDialog extends DialogFragment {
    public Map<String, List> itemsMap;
    public LayoutInflater inflater;
    public CharSequence chosenList;
    public CharSequence chosenListID;
    public void setItemsMap(Map<String, List> items) {
        itemsMap = items;
    }

    static ChooseListDialog newInstance(String[] items) {
        ChooseListDialog c = new ChooseListDialog();
        Bundle args = new Bundle();
        args.putStringArray("items", items);
        c.setArguments(args);

        return c;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final CharSequence[] items = getArguments().getStringArray("items");
        //final String[] items = itemsMap.keySet().toArray(new String[itemsMap.keySet().size()]);
        inflater = getActivity().getLayoutInflater();

        //builder.setView(inflater.inflate(R.layout.choose_list_dialog, null)).setMessage("Choose a list")
        builder.setTitle("Choose a list")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /*LinearLayout rowLink = (LinearLayout) inflater.inflate(R.layout.dialog_addnote, null);
                        //rowLink.setListName("kupa");
                        //Dialog d = (Dialog) getActivity().getFragmentManager().findFragmentById(R.id.choose_list_dialog);

                        ((TextView) getActivity().getLayoutInflater().inflate(R.layout.dialog_addnote, null).findViewById(R.id.chosenListName)).setText(items[which]);
                        //TextView chosenList = (TextView) getDialog().findViewById(R.id.chosenListName);
                        //chosenList.setText(items[which]);
                        CharSequence cosiek = items[which];
                        ((TextView) getActivity().getLayoutInflater().inflate(R.layout.dialog_addnote, null).findViewById(R.id.chosenListID)).setText(itemsMap.get(items[which]).getId());
                        //chosenListID.setText(itemsMap.get(items[which]).getId());
                        getActivity().getLayoutInflater().inflate(R.layout.dialog_addnote, null).findViewById(R.id.chosenListID).invalidate();
                        getActivity().getLayoutInflater().inflate(R.layout.dialog_addnote, null).findViewById(R.id.chosenListName).invalidate();*/
                        //setChosenList(items[which]);
                        //setChosenListID(itemsMap.get(items[which]).getId());

                    }
                });

        return builder.create();
    }

    public CharSequence getChosenListID() {
        return chosenListID;
    }

    public void setChosenListID(CharSequence chosenListID) {
        this.chosenListID = chosenListID;
    }

    public CharSequence getChosenList() {
        return chosenList;
    }

    public void setChosenList(CharSequence chosenList) {
        this.chosenList = chosenList;
    }
}

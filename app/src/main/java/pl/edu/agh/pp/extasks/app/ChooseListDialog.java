package pl.edu.agh.pp.extasks.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.TextView;

import org.trello4j.model.List;

import java.util.Map;


/**
 * Created by Maciek on 2014-10-12.
 */
public class ChooseListDialog extends DialogFragment {
    Map<String, List> itemsMap;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String[] items = getArguments().getStringArray("items");
        //final String[] items = itemsMap.keySet().toArray(new String[itemsMap.keySet().size()]);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.choose_list_dialog, null)).setMessage("Choose a list")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TextView chosenList = (TextView) getDialog().findViewById(R.id.chosenListName);
                        chosenList.setText(items[which]);
                        TextView chosenListID = (TextView) getDialog().findViewById(R.id.chosenListID);
                        chosenListID.setText(itemsMap.get(items[which]).getId());
                    }
                });

        return builder.create();
    }
}

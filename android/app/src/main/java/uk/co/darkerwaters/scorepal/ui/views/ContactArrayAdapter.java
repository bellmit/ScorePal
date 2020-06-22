package uk.co.darkerwaters.scorepal.ui.views;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;

public class ContactArrayAdapter extends ArrayAdapter<String> implements Filterable {

    private final List<String> contacts;
    private final List<String> suggestions;

    private final ContactResolver contactResolver;

    public ContactArrayAdapter(Context context) {
        super(context, R.layout.card_contact);

        this.contactResolver = new ContactResolver(context);
        this.contacts = this.contactResolver.getAllContacts();
        this.suggestions = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_contact, parent, false);
        }
        // set the data on this new card
        setCardData(convertView, getItem(position));
        // and return it
        return convertView;
    }

    private void setCardData(View contactCard, String contactName) {
        // get the extended data for this contact
        String image = this.contactResolver.getContactImage(contactName);
        String email = "";
        String[] contactEmails = this.contactResolver.getContactEmails(contactName);
        if (contactEmails.length > 0) {
            email = contactEmails[0];
        }

        // put the name on the card
        TextView textView = contactCard.findViewById(R.id.ccontName);
        textView.setText(contactName);
        // and the email
        textView = contactCard.findViewById(R.id.ccontNo);
        textView.setText(email);

        // set the default image
        ImageView imageView = contactCard.findViewById(R.id.ccontImage);
        if (null != imageView) {
            if (null != image && !image.isEmpty()) {
                // there is an image URI - use the image for niceness
                imageView.setImageURI(Uri.parse(image));
            }
            else {
                // just use the icon
                imageView.setImageResource(R.drawable.ic_person_black_24dp);
            }
        }
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    private Filter contactFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return (String) resultValue;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                String filterString = constraint.toString().toLowerCase();
                for (String contactName : contacts) {
                    if (contactName.toLowerCase().contains(filterString)) {
                        suggestions.add(contactName);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<String> resultList = (ArrayList<String>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (String contactName : resultList) {
                    add(contactName);
                    notifyDataSetChanged();
                }
            }
        }
    };
}

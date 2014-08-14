package com.rapidftr.activity;

import android.widget.ListView;
import com.rapidftr.R;
import com.rapidftr.adapter.HighlightedFieldsViewAdapter;
import com.rapidftr.model.Enquiry;
import org.json.JSONException;

import java.util.List;

public class ViewAllEnquiryActivity extends BaseEnquiryActivity {
    @Override
    protected void initializeView() {
        setContentView(R.layout.activity_view_all_enquiries);
        try {
            List<Enquiry> enquiries = enquiryRepository.all();
            listView(enquiries);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void listView(List<Enquiry> enquiries) {
        HighlightedFieldsViewAdapter highlightedFieldsViewAdapter = new HighlightedFieldsViewAdapter(this, enquiries, Enquiry.ENQUIRY_FORM_NAME, ViewEnquiryActivity.class);
        ListView enquiryListView = (ListView) findViewById(R.id.enquiry_list);
        if (enquiries.isEmpty()) {
            enquiryListView.setEmptyView(findViewById(R.id.no_enquiry_view));
        }
        enquiryListView.setAdapter(highlightedFieldsViewAdapter);
    }

    @Override
    protected void initializePager() {
    }

    @Override
    protected void initializeSpinner() {
    }
}

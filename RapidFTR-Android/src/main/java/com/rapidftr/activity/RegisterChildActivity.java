package com.rapidftr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.rapidftr.R;
import com.rapidftr.RapidFtrApplication;
import com.rapidftr.dao.ChildDAO;
import com.rapidftr.forms.FormSection;
import com.rapidftr.model.Child;
import com.rapidftr.task.SaveChildAsyncTask;
import com.rapidftr.utils.BitmapUtil;
import com.rapidftr.view.FormSectionView;
import com.rapidftr.view.fields.PhotoUploadBox;
import org.json.JSONException;

import java.util.List;

public class RegisterChildActivity extends RapidFtrActivity {

    protected List<FormSection> formSections = null;

    protected Child child = new Child();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initialize();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("child_state")) {
            try {
                child = new Child(savedInstanceState.getString("child_state"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("child_state", child.toString());
    }

    protected void initialize() {
        setContentView(R.layout.activity_register_child);

        initializeData();
        initializePager();
        initializeSpinner();
        initializeListeners();
    }

    protected void initializeData() {
        this.formSections = getContext().getFormSections();
    }

    protected void initializeListeners() {
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!child.isValid()) {
                        makeToast(R.string.save_child_invalid);
                        return;
                    }

                    child.setOwner(RapidFtrApplication.getInstance().getUserName());
                    child.generateUniqueId();
                    final String childId = child.getId();

                    SaveChildAsyncTask task = new SaveChildAsyncTask(getInjector().getInstance(ChildDAO.class), RegisterChildActivity.this) {
                        @Override
                        protected void onSuccess() {
                            Intent intent = new Intent(RegisterChildActivity.this, ViewChildActivity.class);
                            intent.putExtra("id", childId);
                            finish();
                            startActivity(intent);
                        }
                    };

                    task.execute(child);
                 } catch (Exception e) {
                    makeToast(R.string.internal_error);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PhotoUploadBox.CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            try {
                String fieldId = getIntent().getStringExtra("field_id");
                child.setImage(fieldId, BitmapUtil.getTempStorageFile());

                View fieldView = getPager().findViewWithTag(fieldId);
                if (fieldView != null) {
                    ((PhotoUploadBox) fieldView).repaint();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected Spinner getSpinner() {
        return ((Spinner) findViewById(R.id.spinner));
    }

    protected ViewPager getPager() {
        return (ViewPager) findViewById(R.id.pager);
    }

    protected void initializePager() {
        FormSectionPagerAdapter formSectionAdapter = new FormSectionPagerAdapter();
        getPager().setAdapter(formSectionAdapter);
        getPager().setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int i, float v, int i1) { }
            public void onPageScrollStateChanged(int i) { }

            @Override
            public void onPageSelected(int position) {
                getSpinner().setSelection(position);
            }

        });
    }

    protected void initializeSpinner() {
        ArrayAdapter<FormSection> childDetailsFormArrayAdapter = new ArrayAdapter<FormSection>(this, android.R.layout.simple_spinner_item , formSections);
        getSpinner().setAdapter(childDetailsFormArrayAdapter);
        getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getPager().setCurrentItem(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    protected FormSectionView createFormSectionView(int position) {
        FormSectionView view = (FormSectionView) LayoutInflater.from(this).inflate(R.layout.form_section, null);
        FormSection section = formSections.get(position);
        view.initialize(section, child);
        return view;
    }

    protected class FormSectionPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return formSections.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            FormSectionView view = createFormSectionView(position);
            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}

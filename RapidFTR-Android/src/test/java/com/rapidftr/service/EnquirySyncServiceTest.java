package com.rapidftr.service;

import android.content.SharedPreferences;
import com.rapidftr.model.Enquiry;
import com.rapidftr.model.User;
import com.rapidftr.repository.EnquiryRepository;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static com.rapidftr.RapidFtrApplication.LAST_ENQUIRY_SYNC;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnquirySyncServiceTest {

    @Mock
    private EnquiryHttpDao enquiryHttpDao;

    @Mock
    private EnquiryRepository enquiryRepository;

    @Mock
    private SharedPreferences sharedPreferences;

    @Mock private User currentUser;


    @Test
    public void getRecordShouldRetrieveARecordOverHttp() throws Exception {
        String enquiryInternalId = "enquiryInternalId";
        EnquirySyncService enquirySyncService = new EnquirySyncService(sharedPreferences, enquiryHttpDao, enquiryRepository);
        Enquiry expectedEnquiry = new Enquiry("createdBy", "reporterName", new JSONObject("{}"));
        when(enquiryHttpDao.get(enquiryInternalId)).thenReturn(expectedEnquiry);

        final Enquiry downloadedEnquiry = enquirySyncService.getRecord(enquiryInternalId);

        assertThat(downloadedEnquiry.getUniqueId(), is(expectedEnquiry.getUniqueId()));
    }

    @Test
    public void getIdsToDownloadShouldRetrieveUrlsFromApiSinceLastUpdate() throws Exception {
        final long lastUpdateMillis = System.currentTimeMillis();
        EnquirySyncService enquirySyncService = new EnquirySyncService(sharedPreferences, enquiryHttpDao, enquiryRepository);
        when(sharedPreferences.getLong(LAST_ENQUIRY_SYNC, 0)).thenReturn(lastUpdateMillis);
        when(enquiryHttpDao.getIdsOfUpdated(new DateTime(lastUpdateMillis))).thenReturn(Arrays.asList("blah.com/123", "blah.com/234"));

        List<String> enquiryIds = enquirySyncService.getIdsToDownload();

        assertThat(enquiryIds.get(0), is("blah.com/123"));
        assertThat(enquiryIds.get(1), is("blah.com/234"));
    }

    @Test
    public void shouldUpdateEnquiryWhenItIsNotNew() throws Exception {
        Enquiry enquiry = mock(Enquiry.class);
        Enquiry returnedEnquiry = mock(Enquiry.class);

        when(enquiry.isNew()).thenReturn(false);
        when(enquiryHttpDao.update(enquiry)).thenReturn(returnedEnquiry);

        new EnquirySyncService(sharedPreferences, enquiryHttpDao, enquiryRepository).sync(enquiry, currentUser);

        verify(enquiryRepository).update(returnedEnquiry);
    }

    @Test
    public void shouldCreateEnquiryWhenItIsNew() throws Exception {
        Enquiry enquiry = mock(Enquiry.class);
        Enquiry returnedEnquiry = mock(Enquiry.class);

        when(enquiry.isNew()).thenReturn(true);
        when(enquiryHttpDao.create(enquiry)).thenReturn(returnedEnquiry);

        new EnquirySyncService(sharedPreferences, enquiryHttpDao, enquiryRepository).sync(enquiry, currentUser);

        verify(enquiryRepository).update(returnedEnquiry);
    }


}

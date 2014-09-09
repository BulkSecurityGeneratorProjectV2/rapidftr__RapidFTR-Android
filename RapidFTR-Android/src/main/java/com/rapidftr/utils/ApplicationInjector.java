package com.rapidftr.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.rapidftr.RapidFtrApplication;
import com.rapidftr.database.DatabaseHelper;
import com.rapidftr.database.DatabaseSession;
import com.rapidftr.database.SQLCipherHelper;
import com.rapidftr.model.Child;
import com.rapidftr.model.Enquiry;
import com.rapidftr.model.PotentialMatch;
import com.rapidftr.model.User;
import com.rapidftr.repository.ChildRepository;
import com.rapidftr.repository.EnquiryRepository;
import com.rapidftr.repository.PotentialMatchRepository;
import com.rapidftr.repository.Repository;
import com.rapidftr.service.*;
import com.rapidftr.task.RegisterUnverifiedUserAsyncTask;
import com.rapidftr.task.SyncAllDataAsyncTask;
import com.rapidftr.task.SyncUnverifiedDataAsyncTask;
import com.rapidftr.task.SynchronisationAsyncTask;
import com.rapidftr.utils.http.FluentRequest;
import org.json.JSONException;

public class ApplicationInjector extends AbstractModule {

    @Override
    protected void configure() {
        bind(Context.class).to(RapidFtrApplication.class);
        bind(DatabaseHelper.class).to(SQLCipherHelper.class);
        bind(new TypeLiteral<Repository<Child>>() {
        }).to(ChildRepository.class);
        bind(new TypeLiteral<Repository<Enquiry>>() {
        }).to(EnquiryRepository.class);
        bind(new TypeLiteral<Repository<PotentialMatch>>() {
        }).to(PotentialMatchRepository.class);
        bind(FormService.class);
        bind(RegisterUserService.class);
        bind(RegisterUnverifiedUserAsyncTask.class);
        bind(FluentRequest.class);
        bind(new TypeLiteral<SyncService<Child>>() {
        }).to(ChildSyncService.class);
        bind(new TypeLiteral<SyncService<Enquiry>>() {
        }).to(EnquirySyncService.class);
        bind(new TypeLiteral<SyncService<PotentialMatch>>() {
        }).to(PotentialMatchSyncService.class);

        bind(LogOutService.class);
        bind(LoginService.class);
        bind(DeviceService.class);
    }

    @Provides
    @Named("USER_NAME")
    public String getUserName(User user) {
        return user.getUserName();
    }

    @Provides
    public RapidFtrApplication getRapidFTRApplication() {
        return RapidFtrApplication.getApplicationInstance();
    }

    @Provides
    public DatabaseSession getDatabaseSession(DatabaseHelper helper) {
        return helper.getSession();
    }

    @Provides
    public User getUser(RapidFtrApplication application) throws JSONException {
        return application.isLoggedIn() ? application.getCurrentUser() : null;
    }

    @Provides
    public SynchronisationAsyncTask<Child> getChildSynchronisationAsyncTask(User user, Provider<SyncAllDataAsyncTask<Child>> provider1, Provider<SyncUnverifiedDataAsyncTask<Child>> provider2) {
        return user.isVerified() ? provider1.get() : provider2.get();
    }

    @Provides
    public SynchronisationAsyncTask<Enquiry> getEnquirySynchronisationAsyncTask(User user, Provider<SyncAllDataAsyncTask<Enquiry>> provider1, Provider<SyncUnverifiedDataAsyncTask<Enquiry>> provider2) {
        return user.isVerified() ? provider1.get() : provider2.get();
    }

    @Provides
    public SynchronisationAsyncTask<PotentialMatch> getPotentialMatchSynchronisationAsyncTask(User user, Provider<SyncAllDataAsyncTask<PotentialMatch>> provider1, Provider<SyncUnverifiedDataAsyncTask<PotentialMatch>> provider2) {
        return user.isVerified() ? provider1.get() : provider2.get();
    }

    @Provides
    public SharedPreferences getSharedPreferences() {
        return RapidFtrApplication.getApplicationInstance().getSharedPreferences();
    }

    @Provides
    public EntityHttpDao<Enquiry> getEnquiryHttpDao(RapidFtrApplication rapidFtrApplication) {
        return EntityHttpDaoFactory.createEnquiryHttpDao(rapidFtrApplication.getCurrentUser().getServerUrl(),
                EnquirySyncService.ENQUIRIES_API_PATH,
                EnquirySyncService.ENQUIRIES_API_PARAMETER);
    }

    @Provides
    public EntityHttpDao<PotentialMatch> getPotentialMatchesHttpDao(RapidFtrApplication rapidFtrApplication) {
        return EntityHttpDaoFactory.createPotentialMatchHttpDao(rapidFtrApplication.getCurrentUser().getServerUrl(), PotentialMatchSyncService.POTENTIAL_MATCH_API_PATH,
                PotentialMatchSyncService.POTENTIAL_MATCH_API_PARAMETER);
    }

}

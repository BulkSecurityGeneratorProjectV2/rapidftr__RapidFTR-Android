package com.rapidftr.service;

import com.rapidftr.model.BaseModel;
import com.rapidftr.model.User;
import org.apache.http.HttpException;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public interface SyncService<T extends BaseModel> {

    public T sync(T record, User currentUser) throws IOException, JSONException, HttpException;

    public T getRecord(String id) throws IOException, JSONException, HttpException;

    public List<String> getIdsToDownload() throws IOException, JSONException, HttpException;

    public void setMedia(T t) throws IOException, JSONException;

    public int getNotificationId();

    public String getNotificationTitle();

    void setLastSyncedAt();
}

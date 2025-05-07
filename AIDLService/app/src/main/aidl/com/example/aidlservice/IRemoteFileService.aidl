// IRemoteFileService.aidl
package com.example.aidlservice;

import com.example.aidlservice.FileItem;

interface IRemoteFileService {
    boolean register(String username, String password);
    boolean login(String username, String password);
    List<FileItem> getAccessibleFiles(String username);
    void logAction(String username, String message);
    void addFileForUser(String username, in FileItem file);
    void updateFileName(String username, String fileUri, String newName);
}
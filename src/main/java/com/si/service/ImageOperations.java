package com.si.service;

import com.si.entity.Intuition;
import com.si.framework.FileInfo;

import java.util.List;

/**
 * @author wstevens
 */
public interface ImageOperations
{
    public void init();
    public boolean isInitialized();
    public String storeProfileImage(String username, FileInfo fileInfo);
    public List<String> storeIntuitionImages(String username, String intuitionId, List<FileInfo> fileInfos);
    public List<String> storeIntuitionOutcomeImages(String username, String intuitionId, List<FileInfo> fileInfos);
    public void removeIntuitionImages(String username, Intuition intuition);
    public String copyDefaultProfileImageToUser(String username);
}

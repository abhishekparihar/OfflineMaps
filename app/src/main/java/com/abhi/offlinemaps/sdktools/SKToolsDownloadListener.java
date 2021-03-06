package com.abhi.offlinemaps.sdktools;

/**
 * Listener for download component
 * Must be implemented by anyone who uses download sdk tools component
 */
public interface SKToolsDownloadListener {

    public void onDownloadProgress(SKToolsDownloadItem currentDownloadItem);

    public void onDownloadCancelled(String currentDownloadItemCode);

    public void onDownloadPaused(SKToolsDownloadItem currentDownloadItem);

    public void onInternetConnectionFailed(SKToolsDownloadItem currentDownloadItem, boolean responseReceivedFromServer);

    public void onAllDownloadsCancelled();

    public void onNotEnoughMemoryOnCurrentStorage(SKToolsDownloadItem currentDownloadItem);

    public void onInstallStarted(SKToolsDownloadItem currentInstallingItem);

    public void onInstallFinished(SKToolsDownloadItem currentInstallingItem);
}
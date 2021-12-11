
package xtrem.download.mobile.core.model;

import xtrem.download.mobile.core.model.data.DownloadResult;

import java.util.concurrent.Callable;

interface DownloadThread extends Callable<DownloadResult>
{
    void requestStop();

    void requestPause();

    boolean isRunning();
}

package manatee.demo.loggerdemo.viewer.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

import manatee.demo.loggerdemo.viewer.shared.LogMessage;


public interface LogMessageServiceAsync
{
    void getLogMessages(FilterPagingLoadConfig config, AsyncCallback<PagingLoadResult<LogMessage>> callback);
}

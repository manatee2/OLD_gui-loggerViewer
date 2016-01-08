package manatee.demo.loggerdemo.viewer.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

import manatee.demo.loggerdemo.viewer.shared.LogMessage;


@RemoteServiceRelativePath("logMessages")
public interface LogMessageService extends RemoteService
{
    PagingLoadResult<LogMessage> getLogMessages(FilterPagingLoadConfig config);
}

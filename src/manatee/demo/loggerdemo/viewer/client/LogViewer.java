package manatee.demo.loggerdemo.viewer.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.DateFilter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.ListFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

import manatee.demo.loggerdemo.viewer.shared.LogMessage;
import manatee.demo.loggerdemo.viewer.shared.LogMessage.Severity;


/**
 * Entry point class.
 * 
 * For details of the GXT Proxy-Loader-StoreLoader, see http://blog.christianposta.com/untangling-gxt-storeloader/.
 */
public class LogViewer implements EntryPoint
{
    /**
     * Entry point method.
     */
    @Override
    public void onModuleLoad()
    {
        //
        // asdf
        //
        final LogMessageProperties properties = GWT.create(LogMessageProperties.class);

        //
        // Instantiate the GWT RPC Service.
        //
        final LogMessageServiceAsync service = GWT.create(LogMessageService.class);

        //
        // Instantiate the GXT Proxy which will fetch the data using the GWT RPC Service.
        //
        final RpcProxy<FilterPagingLoadConfig, PagingLoadResult<LogMessage>> rpxProxy =
                new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<LogMessage>>()
                {
                    @Override
                    public void load(FilterPagingLoadConfig loadConfig,
                            AsyncCallback<PagingLoadResult<LogMessage>> callback)
                    {
                        service.getLogMessages(loadConfig, callback);
                    }
                };

        //
        // Instantiate the Client-Side Data Store.
        //
        final ListStore<LogMessage> store = new ListStore<LogMessage>(properties.key());

        //
        // Instantiate the Loader which maps RPC Results to the Client-Side Data Store.
        //
        final PagingLoader<FilterPagingLoadConfig, PagingLoadResult<LogMessage>> remoteLoader =
                new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<LogMessage>>(rpxProxy);
        remoteLoader.useLoadConfig(new FilterPagingLoadConfigBean());
        remoteLoader.setRemoteSort(true);
        remoteLoader.addLoadHandler(
                new LoadResultListStoreBinding<FilterPagingLoadConfig, LogMessage, PagingLoadResult<LogMessage>>(
                        store));

        //
        // Define each of the Columns.
        //

        final List<ColumnConfig<LogMessage, ?>> columnConfig = new ArrayList<ColumnConfig<LogMessage, ?>>();

        final ColumnConfig<LogMessage, Date> timestampColumn =
                new ColumnConfig<LogMessage, Date>(properties.timestamp(), 175, "Timestamp");
        columnConfig.add(timestampColumn);

        final ColumnConfig<LogMessage, String> reporterColumn =
                new ColumnConfig<LogMessage, String>(properties.reporter(), 100, "Reporter");
        columnConfig.add(reporterColumn);

        final ColumnConfig<LogMessage, Severity> severityColumn =
                new ColumnConfig<LogMessage, Severity>(properties.severity(), 100, "Severity");
        columnConfig.add(severityColumn);

        final ColumnConfig<LogMessage, String> textColumn =
                new ColumnConfig<LogMessage, String>(properties.text(), 500, "Text");
        columnConfig.add(textColumn);

        ColumnModel<LogMessage> cm = new ColumnModel<LogMessage>(columnConfig);

        //
        // Define the Grid.
        //
        final Grid<LogMessage> grid = new Grid<LogMessage>(store, cm)
        {
            //
            // Asynchronously fetch the data.
            //
            @Override
            protected void onAfterFirstAttach()
            {
                super.onAfterFirstAttach();
                Scheduler.get().scheduleDeferred(new ScheduledCommand()
                {
                    @Override
                    public void execute()
                    {
                        remoteLoader.load();
                    }
                });
            }
        };
        grid.setLoader(remoteLoader);
        grid.getView().setAutoExpandColumn(textColumn);
        grid.getView().setStripeRows(true);
        grid.getView().setColumnLines(true);

        //
        // Allow Columns to be Filtered-out.
        //

        GridFilters<LogMessage> filters = new GridFilters<LogMessage>(remoteLoader);
        filters.initPlugin(grid);

        DateFilter<LogMessage> dateFilter = new DateFilter<LogMessage>(properties.timestamp());
        filters.addFilter(dateFilter);

        ListStore<Severity> severityStore = new ListStore<Severity>(new ModelKeyProvider<Severity>()
        {
            @Override
            public String getKey(Severity item)
            {
                return item.toString();
            }
        });
        for (Severity severity : LogMessage.Severity.values())
        {
            severityStore.add(severity);
        }
        ListFilter<LogMessage, Severity> severityFilter =
                new ListFilter<LogMessage, Severity>(properties.severity(), severityStore);
        filters.addFilter(severityFilter);

        StringFilter<LogMessage> reporterFilter = new StringFilter<LogMessage>(properties.reporter());
        filters.addFilter(reporterFilter);

        StringFilter<LogMessage> textFilter = new StringFilter<LogMessage>(properties.text());
        filters.addFilter(textFilter);

        //
        // Define the Toolbar used to page through the results.
        //
        final PagingToolBar toolBar = new PagingToolBar(15);
        toolBar.setBorders(false);
        toolBar.bind(remoteLoader);

        //
        // Assemble the Toolbar and Grid.
        //
        VerticalLayoutContainer verticalLayoutContainer = new VerticalLayoutContainer();
        verticalLayoutContainer.add(toolBar);
        verticalLayoutContainer.add(grid);

        //
        // Attach it all to the HTML host page.
        //
        final VerticalLayoutContainer mainPanel = new VerticalLayoutContainer();
        mainPanel.add(verticalLayoutContainer);
        RootPanel.get("logmessagelist").add(mainPanel);
    }
}
